package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;

@NodeChildren({@NodeChild("array"),@NodeChild("expressionNode")})
public abstract class CoverReadLongArrayValueNode extends CoverTypedExpressionNode {
    @Specialization
    public double readLong(VirtualFrame frame, double[] array, long index) {
        return array[(int) index];
    }
    
    @Override
    public CoverType getType() {
        return CoverType.DOUBLE;
    }    
}
