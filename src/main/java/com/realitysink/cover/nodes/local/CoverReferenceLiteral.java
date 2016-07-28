package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.SLExpressionNode;

public class CoverReferenceLiteral extends SLExpressionNode {
    final CoverReference ref;

    public CoverReferenceLiteral(CoverReference ref) {
        this.ref = ref;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return ref;
    }
}
