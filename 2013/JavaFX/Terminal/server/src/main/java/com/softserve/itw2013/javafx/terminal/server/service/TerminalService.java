package com.softserve.itw2013.javafx.terminal.server.service;

import com.softserve.itw2013.javafx.terminal.data.Terminal;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
public interface TerminalService {
    List<Terminal> getAllTerminals();
}
