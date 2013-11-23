package com.softserve.itw2013.javafx.terminal.client.controller;

import com.softserve.itw2013.javafx.terminal.client.TerminalClientContext;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import com.softserve.itw2013.javafx.terminal.data.CashTransaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 07.11.13
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class WithdrawController extends AbstractJFXController {
    @FXML
    private Label lblBalance;

    @FXML
    private Button btnWithdraw1;
    @FXML
    private Button btnWithdraw2;
    @FXML
    private Button btnWithdraw3;
    @FXML
    private Button btnWithdraw4;
    @FXML
    private Button btnWithdraw5;
    @FXML
    private Button btnWithdraw6;
    @FXML
    private Button btnWithdraw7;

    private final Map<Button, BigDecimal> buttonWithdraws = new HashMap<Button, BigDecimal>();

    public WithdrawController() {
        super(true);
    }

    @FXML
    public void initialize() {
        buttonWithdraws.put(btnWithdraw1, new BigDecimal(btnWithdraw1.getText()));
        buttonWithdraws.put(btnWithdraw2, new BigDecimal(btnWithdraw2.getText()));
        buttonWithdraws.put(btnWithdraw3, new BigDecimal(btnWithdraw3.getText()));
        buttonWithdraws.put(btnWithdraw4, new BigDecimal(btnWithdraw4.getText()));
        buttonWithdraws.put(btnWithdraw5, new BigDecimal(btnWithdraw5.getText()));
        buttonWithdraws.put(btnWithdraw6, new BigDecimal(btnWithdraw6.getText()));
        buttonWithdraws.put(btnWithdraw7, new BigDecimal(btnWithdraw7.getText()));
    }

    @Override
    public void open(final Stage owner, final boolean modal) {
        Validate.notNull(owner);

        try {
            final BigDecimal accountBalance = TerminalClientContext.getTerminalDispatcher().getAccountBalance(TerminalClientContext.getCardInfo().getAccountUUID());

            lblBalance.setText(accountBalance.toString());

            for (Map.Entry<Button, BigDecimal> buttonWithdraw : buttonWithdraws.entrySet()) {
                buttonWithdraw.getKey().setDisable(buttonWithdraw.getValue().compareTo(accountBalance) > 0);
            }

            super.open(owner, true);
        } catch (Exception exception) {
            MessageBox.show(getStage(), exception.getMessage(), "Get account valance failure", MessageBox.ICON_ERROR | MessageBox.OK);
        }
    }

    @FXML
    private void onWithdraw(final ActionEvent event) {
        final BigDecimal withdrawAmount = buttonWithdraws.get((Button) event.getSource());

        switch (MessageBox.show(getStage(), String.format("Are you sure to withdraw %s y.e.?", withdrawAmount), "Confirm withdraw", MessageBox.ICON_QUESTION | MessageBox.YES | MessageBox.NO | MessageBox.CANCEL)) {
            case MessageBox.YES:
                try {
                    final CashTransaction cashTransaction = TerminalClientContext.getTerminalDispatcher().withdrawCash(TerminalClientContext.getCardInfo().getAccountUUID(), TerminalClientContext.getTerminalUuid(), withdrawAmount);

                    MessageBox.show(getStage(), String.format("Grab the money.\n\nResult balance is %s y.e.\n\nPlease, do NOT forget your card!\n\nAnd...run, baby, run!!!", cashTransaction.getResultBalance()), "Transaction complete success", MessageBox.ICON_INFORMATION | MessageBox.OK);
                } catch (Exception exception) {
                    MessageBox.show(getStage(), exception.getMessage(), "Cash withdraw failure", MessageBox.ICON_ERROR | MessageBox.OK);
                }
            case MessageBox.CANCEL:
                hide();
                break;
            case MessageBox.NO:
                break;
        }
    }

    @FXML
    private void onEscape() {
        hide();
    }
}
