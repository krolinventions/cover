package com.realitysink.cover.parser;

public class CoverParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CoverParseException() {
    }

    public CoverParseException(String arg0) {
        super(arg0);
    }

    public CoverParseException(Throwable arg0) {
        super(arg0);
    }

    public CoverParseException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public CoverParseException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

}
