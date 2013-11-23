package com.softserve.itw2013.javafx.terminal.client.controller;

import com.softserve.itw2013.javafx.terminal.client.TerminalClientContext;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import jfx.messagebox.MessageBox;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 07.11.13
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class CardInController extends AbstractJFXController {
    @FXML
    private LogonController logonController;

    private Service<Integer> waitForCardService;

    public CardInController() {
        super(true);
    }

    @FXML
    public void initialize() throws Exception {
        waitForCardService = new Service<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        return TerminalClientContext.getCardManager().waitForCard();
                    }
                };
            }
        };

        waitForCardService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                cardInserted();

                waitForCard();
            }
        });
        waitForCardService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                MessageBox.show(getStage(), waitForCardService.getException().getMessage(), "Card wait failure", MessageBox.ICON_ERROR | MessageBox.OK);

                waitForCard();
            }
        });

        waitForCard();
    }

    private void waitForCard() {
        waitForCardService.restart();
    }

    private void cardInserted() {
        final Integer cardId = waitForCardService.getValue();

        if (cardId != null) {
            TerminalClientContext.setCardId(cardId);

            try {
                open(logonController, true);
            } finally {
                TerminalClientContext.setCardId(null);
            }
        } else {
            MessageBox.show(getStage(), "Unrecognized or incorrect card is used!", "Card check failure. Please, remove or use another card", MessageBox.ICON_ERROR | MessageBox.OK);
        }
    }

    @FXML
    private void exit() {
        Platform.exit();
    }
}
