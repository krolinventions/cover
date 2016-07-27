package com.realitysink.cover.nodes;

import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * An expression that doesn't do anything. It returns null.
 * This is useful when you need to return an expression but don't have anything useful to do at runtime.
 */
public class CoverNopExpression extends SLExpressionNode {
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return null;
    }
}
