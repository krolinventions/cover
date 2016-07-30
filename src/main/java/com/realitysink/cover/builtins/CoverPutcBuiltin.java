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

import java.io.IOException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

@NodeInfo(shortName = "putch")
@NodeChildren({@NodeChild("argument"), @NodeChild("file")})
public abstract class CoverPutcBuiltin extends CoverTypedExpressionNode {

    @Specialization
    public long putch(long c, long file) {
        doPutch((byte)c);
        return c;
    }

    @TruffleBoundary
    private void doPutch(byte c) {
        // write(c) is broken? It sometimes silently does NOT write a byte. This workaround does.
        byte[] array = {c};
        try {
            System.out.write(array);
        } catch (IOException e) {
            throw new CoverRuntimeException(this, e);
        }
    }
    
    @Override
    public CoverType getType() {
        return CoverType.VOID;
    }    
}
