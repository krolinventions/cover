package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.SLExpressionNode;

public class CoverFrameSlotLiteral extends SLExpressionNode {
    final FrameSlot frameSlot;

    public CoverFrameSlotLiteral(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return frameSlot;
    }
}
