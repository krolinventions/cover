package com.realitysink.cover.nodes.access;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;

public class CoverCreateObjectNode extends CoverTypedExpressionNode {

    private CoverType type;
    
    public CoverCreateObjectNode(CoverType type) {
        this.type = type;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return type.getShape().newInstance();
    }

    @Override
    public CoverType getType() {
        return type;
    }
}
