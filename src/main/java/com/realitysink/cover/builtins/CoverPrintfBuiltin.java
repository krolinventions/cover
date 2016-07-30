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
package com.realitysink.cover.builtins;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLExpressionNode;

@NodeInfo(shortName = "printf")
public class CoverPrintfBuiltin extends CoverTypedExpressionNode {
    @Children
    private final SLExpressionNode[] arguments;

    public CoverPrintfBuiltin(SLExpressionNode[] arguments) {
        this.arguments = arguments;
    }

    @ExplodeLoop
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        CompilerAsserts.compilationConstant(arguments.length);
        String formatString = (String) arguments[0].executeGeneric(frame);
        Object[] printfArguments = new Object[arguments.length-1];
        for (int i=1;i<arguments.length;i++) {
            printfArguments[i-1] = arguments[i].executeGeneric(frame);
        }
        doPrintf(formatString, printfArguments);
        return null; // is actually a void function
    }

    @TruffleBoundary
    private static void doPrintf(String formatString, Object[] printfArguments) {
        String fixed = formatString.replace("%jd", "%d");
        System.out.format(fixed,printfArguments);
    }

    @Override
    public CoverType getType() {
        return CoverType.VOID;
    }
}
