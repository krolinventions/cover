package com.realitysink.cover.local;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

public class CoverReadArrayValueNode extends SLExpressionNode {

    private final FrameSlot frameSlot;
    @Child
    private SLExpressionNode expressionNode;
    
    public CoverReadArrayValueNode(FrameSlot frameSlot, SLExpressionNode expressionNode) {
        this.frameSlot = frameSlot;
        this.expressionNode = expressionNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object[] array;
        Object object;
        try {
            object = frame.getObject(frameSlot);
        } catch (FrameSlotTypeException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        try {
            array = (Object[]) object;
        } catch (ClassCastException e1) {
            throw new CoverRuntimeException(this, e1);
        }
        try {
            return array[(int) expressionNode.executeLong(frame)];
        } catch (UnexpectedResultException e) {
            throw new CoverRuntimeException(this, e);
        }
    }
}
