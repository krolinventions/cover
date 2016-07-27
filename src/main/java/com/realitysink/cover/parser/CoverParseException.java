package com.realitysink.cover.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CoverParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private IASTNode node;

    public CoverParseException(IASTNode node, String arg0) {
        super(addContext(node, arg0));
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1) {
        super(addContext(node, arg0), arg1);
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(addContext(node, arg0), arg1, arg2, arg3);
        this.setNode(node);
    }

    private static String addContext(IASTNode node, String message) {
        if (node == null) {
            return "<unknown>: " + message;
        }
        IASTFileLocation f = node.getFileLocation();
        return f.getFileName() + ":" + f.getStartingLineNumber() + ": " + message + ": '" + node.getRawSignature() + "'";
    }

    public IASTNode getNode() {
        return node;
    }

    private void setNode(IASTNode node) {
        this.node = node;
    }
}
