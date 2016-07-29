/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.realitysink.cover.nodes.expression;

import java.math.BigInteger;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLBinaryNode;
import com.realitysink.cover.runtime.CoverRuntimeException;
import com.realitysink.cover.runtime.SLFunction;
import com.realitysink.cover.runtime.SLNull;

/**
 * The {@code ==} operator of SL is defined on all types. Therefore, we need a
 * {@link #equal(Object, Object) implementation} that can handle all possible types. But since
 * {@code ==} can only return {@code true} when the type of the left and right operand are the same,
 * the specializations already cover all possible cases that can return {@code true} and the generic
 * case is trivial.
 * <p>
 * Note that we do not need the analogous {@code !=} operator, because we can just
 * {@link SLLogicalNotNode negate} the {@code ==} operator.
 */
@NodeInfo(shortName = "==")
@NodeChildren({@NodeChild("leftNode"), @NodeChild("rightNode")})
public abstract class CoverEqualDoubleNode extends CoverTypedExpressionNode {

    @Specialization
    protected boolean equal(double left, double right) {
        // FIXME: warn if these are not whole numbers!
        // throw new CoverRuntimeException(this, "Really? Are you comparing doubles using == ?");
        return left == right;
    }

    public CoverType getType() {
        return CoverType.BOOLEAN;
    }
}
