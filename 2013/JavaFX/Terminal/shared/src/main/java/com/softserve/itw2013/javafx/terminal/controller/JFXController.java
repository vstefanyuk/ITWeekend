package com.softserve.itw2013.javafx.terminal.controller;

import javafx.stage.Stage;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 0:06
 * To change this template use File | Settings | File Templates.
 */
public interface JFXController {
    void assignStage(Stage stage);

    void open(Stage owner, boolean modal);

    Integer getReturnCode();
}
