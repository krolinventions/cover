package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

@NodeChildren({@NodeChild("frameSlot"),@NodeChild("expressionNode")})
public abstract class CoverReadArrayValueNode extends SLExpressionNode {
    
    @Specialization(guards="isLongArray(frame, frameSlot)")
    public long readLong(VirtualFrame frame, FrameSlot frameSlot, long index) {
        long[] array;
        Object object;
        try {
            object = frame.getObject(frameSlot);
        } catch (FrameSlotTypeException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        try {
            array = (long[]) object;
        } catch (ClassCastException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        return array[(int) index];
    }

    @Specialization
    public double readDouble(VirtualFrame frame, FrameSlot frameSlot, long index) {
        double[] array;
        Object object;
        try {
            object = frame.getObject(frameSlot);
        } catch (FrameSlotTypeException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        try {
            array = (double[]) object;
        } catch (ClassCastException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        return array[(int) index];
    }
    
    protected boolean isLongArray(VirtualFrame frame, FrameSlot frameSlot) {
        try {
            return frame.getObject(frameSlot) instanceof long[];
        } catch (FrameSlotTypeException e) {
            return false;
        }
    }
}
