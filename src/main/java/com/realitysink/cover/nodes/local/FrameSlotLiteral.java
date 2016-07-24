package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.SLExpressionNode;

public class FrameSlotLiteral extends SLExpressionNode {
    final FrameSlot frameSlot;

    public FrameSlotLiteral(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return frameSlot;
    }
}
