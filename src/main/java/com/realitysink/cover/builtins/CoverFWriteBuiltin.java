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
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

/**
 * size_t fwrite ( const void * ptr, size_t size, size_t count, FILE * stream );
 */
@NodeInfo(shortName = "fwrite")
@NodeChildren({@NodeChild("ptr"), @NodeChild("size"), @NodeChild("count"), @NodeChild("stream")})
public abstract class CoverFWriteBuiltin extends CoverTypedExpressionNode {

    // FIXME: should take a byte[] directly!
    @Specialization
    public Object fwrite(VirtualFrame frame, CoverReference ref, long size, long count, long stream) {
        long[] ptr;
        try {
            ptr = (long[]) frame.getObject(ref.getFrameSlot());
        } catch (FrameSlotTypeException e) {
            throw new CoverRuntimeException(this, "invalid ptr argument");
        }
        long totalSize = size * count;
        byte[] bytes = new byte[(int) totalSize];
        for (int i=0;i<totalSize;i++) {
            long value = (long) ptr[i];
            bytes[i] = (byte)value;
        }
        doWrite(bytes, size, count, stream);
        return null; // is actually a void function
    }
    
    @Specialization
    public Object fwrite(long[] ptr, long size, long count, long stream) {
        long totalSize = size * count;
        byte[] bytes = new byte[(int) totalSize];
        for (int i=0;i<totalSize;i++) {
            bytes[i] = (byte) ptr[i];
        }
        doWrite(bytes, size, count, stream);
        return null; // is actually a void function
    }

    @TruffleBoundary
    private void doWrite(byte[] bytes, long size, long count, long stream) {
        // stream is ignored, we always write to stdout
        try {
            System.out.write(bytes);
        } catch (IOException e) {
            throw new CoverRuntimeException(this, e);
        }
    }

    @Override
    public CoverType getType() {
        return CoverType.VOID;
    }
}
