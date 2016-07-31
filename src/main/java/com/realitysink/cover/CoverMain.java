/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.realitysink.cover;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;
import com.realitysink.cover.builtins.SLDefineFunctionBuiltin;
import com.realitysink.cover.builtins.SLNanoTimeBuiltin;
import com.realitysink.cover.builtins.SLPrintlnBuiltin;
import com.realitysink.cover.builtins.SLReadlnBuiltin;
import com.realitysink.cover.builtins.SLStackTraceBuiltin;
import com.realitysink.cover.nodes.SLTypes;
import com.realitysink.cover.nodes.access.SLReadPropertyCacheNode;
import com.realitysink.cover.nodes.access.SLReadPropertyNode;
import com.realitysink.cover.nodes.access.SLWritePropertyCacheNode;
import com.realitysink.cover.nodes.access.SLWritePropertyNode;
import com.realitysink.cover.nodes.call.SLDispatchNode;
import com.realitysink.cover.nodes.call.SLInvokeNode;
import com.realitysink.cover.nodes.controlflow.SLBlockNode;
import com.realitysink.cover.nodes.controlflow.SLBreakNode;
import com.realitysink.cover.nodes.controlflow.SLContinueNode;
import com.realitysink.cover.nodes.controlflow.SLDebuggerNode;
import com.realitysink.cover.nodes.controlflow.SLIfNode;
import com.realitysink.cover.nodes.controlflow.SLReturnNode;
import com.realitysink.cover.nodes.controlflow.SLWhileNode;
import com.realitysink.cover.nodes.expression.SLBigIntegerLiteralNode;
import com.realitysink.cover.nodes.expression.SLEqualNode;
import com.realitysink.cover.nodes.expression.SLFunctionLiteralNode;
import com.realitysink.cover.nodes.expression.SLLogicalAndNode;
import com.realitysink.cover.nodes.expression.SLLogicalOrNode;
import com.realitysink.cover.nodes.expression.SLStringLiteralNode;
import com.realitysink.cover.nodes.expression.SLSubNode;
import com.realitysink.cover.nodes.local.SLReadLocalVariableNode;
import com.realitysink.cover.nodes.local.SLWriteLocalVariableNode;
import com.realitysink.cover.runtime.SLContext;
import com.realitysink.cover.runtime.SLFunction;
import com.realitysink.cover.runtime.SLFunctionRegistry;
import com.realitysink.cover.runtime.SLNull;
import com.realitysink.cover.runtime.SLUndefinedNameException;
import com.realitysink.cover.slparser.SLNodeFactory;
import com.realitysink.cover.slparser.SLParser;
import com.realitysink.cover.slparser.SLScanner;

public final class CoverMain {

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws IOException {
        Source source;
        if (args.length == 0) {
            source = Source.fromReader(new InputStreamReader(System.in), "<stdin>").withMimeType(CoverLanguage.MIME_TYPE);
        } else {
            source = Source.fromFileName(args[0]);
        }

        executeSource(source, System.in, System.out);
    }

    private static void executeSource(Source source, InputStream in, PrintStream out) {
        String runtime = Truffle.getRuntime().getName();
        if (!"Graal Truffle Runtime".equals(runtime)) {
            System.err.println("WARNING: not running on Graal/Truffle but on " + runtime);
        }

        PolyglotEngine engine = PolyglotEngine.newBuilder().setIn(in).setOut(out).build();
        assert engine.getLanguages().containsKey(CoverLanguage.MIME_TYPE);

        try {
            Value result = engine.eval(source);

            if (result == null) {
                throw new SLException("No function main() defined in SL source file.");
            } else if (result.get() != SLNull.SINGLETON) {
                out.println(result.get());
            }

        } catch (Throwable ex) {
            /*
             * PolyglotEngine.eval wraps the actual exception in an IOException, so we have to
             * unwrap here.
             */
            Throwable cause = ex.getCause();
            if (cause instanceof UnsupportedSpecializationException) {
                out.println(formatTypeError((UnsupportedSpecializationException) cause));
                cause.printStackTrace();
            } else if (cause instanceof SLUndefinedNameException) {
                out.println(cause.getMessage());
                cause.printStackTrace();
            } else {
                /* Unexpected error, just print out the full stack trace for debugging purposes. */
                ex.printStackTrace(out);
            }
        }

        engine.dispose();
    }

    /**
     * Provides a user-readable message for run-time type errors. SL is strongly typed, i.e., there
     * are no automatic type conversions of values. Therefore, Truffle does the type checking for
     * us: if no matching node specialization for the actual values is found, then we have a type
     * error. Specialized nodes use the {@link UnsupportedSpecializationException} to report that no
     * specialization was found. We therefore just have to convert the information encapsulated in
     * this exception in a user-readable form.
     */
    public static String formatTypeError(UnsupportedSpecializationException ex) {
        StringBuilder result = new StringBuilder();
        result.append("Type error");
        if (ex.getNode() != null && ex.getNode().getSourceSection() != null) {
            SourceSection ss = ex.getNode().getSourceSection();
            if (ss != null && ss.getSource() != null) {
                result.append(" at ").append(ss.getSource().getShortName()).append(" line ").append(ss.getStartLine()).append(" col ").append(ss.getStartColumn());
            }
        }
        result.append(": operation");
        if (ex.getNode() != null) {
            NodeInfo nodeInfo = SLContext.lookupNodeInfo(ex.getNode().getClass());
            if (nodeInfo != null) {
                result.append(" \"").append(nodeInfo.shortName()).append("\"");
            }
        }
        result.append(" not defined for");

        String sep = " ";
        for (int i = 0; i < ex.getSuppliedValues().length; i++) {
            Object value = ex.getSuppliedValues()[i];
            Node node = ex.getSuppliedNodes()[i];
            if (node != null) {
                result.append(sep);
                sep = ", ";

                if (value instanceof Long || value instanceof BigInteger) {
                    result.append("Number ").append(value);
                } else if (value instanceof Boolean) {
                    result.append("Boolean ").append(value);
                } else if (value instanceof String) {
                    result.append("String \"").append(value).append("\"");
                } else if (value instanceof SLFunction) {
                    result.append("Function ").append(value);
                } else if (value == SLNull.SINGLETON) {
                    result.append("NULL");
                } else if (value == null) {
                    // value is not evaluated because of short circuit evaluation
                    result.append("ANY");
                } else {
                    result.append(value);
                }
            }
        }
        return result.toString();
    }
}
