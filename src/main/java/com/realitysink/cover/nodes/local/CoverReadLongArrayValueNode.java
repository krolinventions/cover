package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

@NodeChildren({@NodeChild("ref"),@NodeChild("expressionNode")})
public abstract class CoverReadLongArrayValueNode extends CoverTypedExpressionNode {
    @Specialization
    public long readLong(VirtualFrame frame, CoverReference ref, long index) {
        long[] array;
        Object object;
        try {
            object = frame.getObject(ref.getFrameSlot());
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
    
    @Override
    public CoverType getType() {
        return CoverType.LONG;
    }    
}
