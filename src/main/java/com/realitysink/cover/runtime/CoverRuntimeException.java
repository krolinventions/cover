package com.realitysink.cover.runtime;

import com.oracle.truffle.api.nodes.Node;

public class CoverRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CoverRuntimeException(Node node, String arg0, Throwable arg1) {
        super(nodeDescription(node) + arg0, arg1);
    }

    public CoverRuntimeException(Node node, String arg0) {
        super(nodeDescription(node) + arg0);
    }

    public CoverRuntimeException(Node node, Throwable arg0) {
        super(nodeDescription(node) + arg0.getMessage(), arg0);
    }
    
    private static String nodeDescription(Node node) {
        return node.getSourceSection().getSource().getName()+":"+node.getSourceSection().getStartLine()+": " + node.getSourceSection().getCode()+": ";
    }
}
