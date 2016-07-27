package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

public class CoverNewArrayNode extends SLExpressionNode {
    private final String typeName;
    @Child
    private SLExpressionNode length;
    
    public CoverNewArrayNode(String typeName, SLExpressionNode length) {
        this.typeName = typeName;
        this.length = length;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        int l;
        try {
            l = (int) length.executeLong(frame);
        } catch (UnexpectedResultException e) {
            throw new CoverRuntimeException(this, "could not create array", e);
        }
        if ("int".equals(typeName)) {
            return new long[l];
        } else if ("long".equals(typeName)) {
            return new long[l];
        } else if ("char".equals(typeName)) {
            return new long[l];
        } else if ("double".equals(typeName)) {
            return new double[l];
        } else {
            throw new CoverRuntimeException(this, "unsupported array type: " + typeName);
        }        
    }
}
