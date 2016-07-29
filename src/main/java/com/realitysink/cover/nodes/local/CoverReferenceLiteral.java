package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;

public class CoverReferenceLiteral extends CoverTypedExpressionNode {
    final CoverReference ref;

    public CoverReferenceLiteral(CoverReference ref) {
        this.ref = ref;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return ref;
    }

    @Override
    public CoverType getType() {
        return ref.getType();
    }
}
