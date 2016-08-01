/*
 * Copyright (c) 2016 Gerard Krol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.realitysink.cover;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Instrument;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;
import com.oracle.truffle.tools.TruffleProfiler;
import com.realitysink.cover.runtime.SLNull;
import com.realitysink.cover.runtime.SLUndefinedNameException;

public final class CoverMain {

    /**
     * The main entry point.
     */
    public static void main(String[] args) throws IOException {
        Source source;
        if (args.length == 0) {
            source = Source.fromReader(new InputStreamReader(System.in), "<stdin>").withMimeType(CoverLanguage.MIME_TYPE);
        } else {
            source = Source.fromFileName(args[0]).withMimeType(CoverLanguage.MIME_TYPE);
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

        Map<String, Instrument> instruments = engine.getInstruments();
        for (String name : instruments.keySet()) {
            System.err.println(name);
        }
        Instrument profiler = instruments.get(CoverProfiler.ID);
        if (profiler == null) {
          System.err.println("Truffle profiler not available. Might be a class path issue");
        }
        //profiler.setEnabled(true);
        // The above doesn't work? It checks for a property:
        //System.setProperty("truffle.profiling.enabled", "true");
        
        try {
            Value result = engine.eval(source);

            if (result == null) {
                throw new SLException("No function main() defined?");
            } else if (result.get() != SLNull.SINGLETON) {
                // out.println(result.get());
            }

        } catch (Throwable ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof UnsupportedSpecializationException) {
                out.println(cause.getMessage());
                cause.printStackTrace();
            } else if (cause instanceof SLUndefinedNameException) {
                out.println(cause.getMessage());
                cause.printStackTrace();
            } else {
                ex.printStackTrace(out);
            }
        }
        System.err.println("Profiler enabled: " + profiler.isEnabled());
        engine.dispose();
    }
}
