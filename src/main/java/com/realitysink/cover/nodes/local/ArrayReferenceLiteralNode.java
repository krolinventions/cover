package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.realitysink.cover.nodes.CoverReference;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

public class ArrayReferenceLiteralNode extends SLExpressionNode {
    @Child
    private SLExpressionNode index;
    
    private final CoverReference ref;
    
    public ArrayReferenceLiteralNode(CoverReference ref, SLExpressionNode index) {
        this.ref = ref;
        this.index = index;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        int i;
        try {
            i = (int) index.executeLong(frame);
        } catch (UnexpectedResultException e) {
            throw new CoverRuntimeException(this, e);
        }
        return ref.getArrayMember(i);
    }
}
