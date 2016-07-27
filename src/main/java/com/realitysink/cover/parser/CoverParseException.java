package com.realitysink.cover.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CoverParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private IASTNode node;

    public CoverParseException(IASTNode node, String arg0) {
        super(CoverParser.nodeMessage(node, arg0));
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1) {
        super(CoverParser.nodeMessage(node, arg0), arg1);
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(CoverParser.nodeMessage(node, arg0), arg1, arg2, arg3);
        this.setNode(node);
    }

    public IASTNode getNode() {
        return node;
    }

    private void setNode(IASTNode node) {
        this.node = node;
    }
}
