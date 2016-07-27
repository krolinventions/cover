package com.realitysink.cover.parser;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CoverParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CoverParseException(IASTNode node, String arg0) {
        super(addContext(node, arg0));
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1) {
        super(addContext(node, arg0), arg1);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(addContext(node, arg0), arg1, arg2, arg3);
    }

    private static String addContext(IASTNode node, String message) {
        if (node == null) {
            return "<unknown>: " + message;
        }
        IASTFileLocation f = node.getFileLocation();
        return f.getFileName() + ":" + f.getStartingLineNumber() + ": " + message + ": '" + node.getRawSignature() + "'";
    }
}
