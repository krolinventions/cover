package com.realitysink.cover.local;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.nodes.local.ArrayReference;

public class ArrayReferenceLiteralNode extends SLExpressionNode {
    @Child
    private SLExpressionNode index;
    
    private final FrameSlot frameSlot;
    
    public ArrayReferenceLiteralNode(FrameSlot frameSlot, SLExpressionNode index) {
        this.frameSlot = frameSlot;
        this.index = index;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        try {
            return new ArrayReference(frameSlot, index.executeLong(frame));
        } catch (UnexpectedResultException e) {
            throw new RuntimeException(e);
        }
    }
}
