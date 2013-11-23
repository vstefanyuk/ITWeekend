package com.softserve.itw2013.javafx.terminal.admin;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 16.11.13
 * Time: 0:07
 * To change this template use File | Settings | File Templates.
 */
public class TerminalAdminContext {
    private static TerminalDispatcher terminalDispatcher;

    public static TerminalDispatcher getTerminalDispatcher() {
        return terminalDispatcher;
    }

    static void setTerminalDispatcher(TerminalDispatcher terminalDispatcher) {
        TerminalAdminContext.terminalDispatcher = terminalDispatcher;
    }
}
