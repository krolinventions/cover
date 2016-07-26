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
package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

@NodeChildren({@NodeChild("destination"), @NodeChild("value")})
@NodeInfo(shortName="=")
public abstract class CoverWriteVariableNode extends SLExpressionNode {
    @Specialization(guards="frameSlotIsObject(arrayReference)")
    protected Object writeLong(VirtualFrame frame, ArrayReference arrayReference, long value) {
        Object[] array = (Object[]) frame.getValue(arrayReference.getFrameSlot());
        try {
            array[(int) arrayReference.getIndex()] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CoverRuntimeException(this, "index " + arrayReference.getIndex() + " out of bounds for " + arrayReference.getFrameSlot().getIdentifier());
        }
        return value;
    }
    
    @Specialization(guards="frameSlotIsObject(arrayReference)")
    protected Object writeDouble(VirtualFrame frame, ArrayReference arrayReference, double value) {
        Object[] array = (Object[]) frame.getValue(arrayReference.getFrameSlot());
        try {
            array[(int) arrayReference.getIndex()] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CoverRuntimeException(this, "index " + arrayReference.getIndex() + " out of bounds for " + arrayReference.getFrameSlot().getIdentifier());
        }
        return value;
    }

    @Specialization(guards="frameSlotIsObject(arrayReference)")
    protected Object writeObject(VirtualFrame frame, ArrayReference arrayReference, Object value) {
        Object[] array = (Object[]) frame.getValue(arrayReference.getFrameSlot());
        try {
            array[(int) arrayReference.getIndex()] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CoverRuntimeException(this, "index " + arrayReference.getIndex() + " out of bounds for " + arrayReference.getFrameSlot().getIdentifier());
        }
        return value;
    }
    
    protected boolean frameSlotIsObject(ArrayReference arrayReference) {
        return isObjectOrIllegal(arrayReference.getFrameSlot());
    }

    @Specialization(guards = "isLongOrIllegal(frameSlot)")
    protected long writeLong(VirtualFrame frame, FrameSlot frameSlot, long value) {
        frameSlot.setKind(FrameSlotKind.Long);
        frame.setLong(frameSlot, value);
        return value;
    }

    @Specialization(guards = "isDoubleOrIllegal(frameSlot)")
    protected double writeDouble(VirtualFrame frame, FrameSlot frameSlot, double value) {
        //System.err.println("setting " + frameSlot.getIdentifier() + " to double " + value);
        frameSlot.setKind(FrameSlotKind.Double);
        frame.setDouble(frameSlot, value);
        return value;
    }

    @Specialization(contains = {"writeLong", "writeDouble"},
            guards = {"isObjectOrIllegal(frameSlot)", "isNotBoxed(value)"})
    protected Object write(VirtualFrame frame, FrameSlot frameSlot, Object value) {
        //System.err.println("setting " + frameSlot.getIdentifier() + " to object " + value);
        frameSlot.setKind(FrameSlotKind.Object);
        frame.setObject(frameSlot, value);
        return value;
    }
    
    protected boolean isLongOrIllegal(FrameSlot frameSlot) {
        //System.err.println("isLongOrIllegal:"+ frameSlot.getIdentifier() + " has type " + frameSlot.getKind());
        return frameSlot.getKind() == FrameSlotKind.Long || frameSlot.getKind() == FrameSlotKind.Illegal;
    }    
    protected boolean isDoubleOrIllegal(FrameSlot frameSlot) {
        //System.err.println("isDoubleOrIllegal:"+ frameSlot.getIdentifier() + " has type " + frameSlot.getKind());
        return frameSlot.getKind() == FrameSlotKind.Double || frameSlot.getKind() == FrameSlotKind.Illegal;
    }    
    protected boolean isObjectOrIllegal(FrameSlot frameSlot) {
        //System.err.println("isObjectOrIllegal:"+ frameSlot.getIdentifier() + " has type " + frameSlot.getKind());
        return frameSlot.getKind() == FrameSlotKind.Object || frameSlot.getKind() == FrameSlotKind.Illegal;
    }    
    protected boolean isNotBoxed(Object value) {
        if (value instanceof Long) return false;
        if (value instanceof Double) return false;
        return true;
    }
}
