package com.softserve.itw2013.javafx.terminal.exception;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 18.11.13
 * Time: 1:51
 * To change this template use File | Settings | File Templates.
 */
public class TerminalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TerminalException(String message) {
        super(message);
    }

    public TerminalException(Throwable cause) {
        super(cause);
    }

    public TerminalException(String message, Throwable cause) {
        super(message, cause);
    }
}
