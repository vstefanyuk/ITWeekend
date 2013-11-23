package com.softserve.itw2013.javafx.terminal.client.controller;

import com.softserve.itw2013.javafx.terminal.cardman.CardInfo;
import com.softserve.itw2013.javafx.terminal.client.TerminalClientContext;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import org.apache.commons.lang3.Validate;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 04.11.13
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
public class HomeController extends AbstractJFXController {
    @FXML
    private WithdrawController withdrawController;

    @FXML
    private Label lblOwnerName;

    public HomeController() {
        super(true);
    }

    @FXML
    public void initialize() throws Exception {
        final Tooltip tlpOwnerName = new Tooltip();

        tlpOwnerName.setFont(lblOwnerName.getFont());

        lblOwnerName.setTooltip(tlpOwnerName);
    }

    @Override
    public void open(final Stage owner, final boolean modal) {
        Validate.notNull(owner);

        try {
            final CardInfo cardInfo = TerminalClientContext.getCardManager().getCardInfo(TerminalClientContext.getCardId());

            if (cardInfo != null) {
                TerminalClientContext.setCardInfo(cardInfo);

                lblOwnerName.setText(cardInfo.getOwnerName());
                lblOwnerName.getTooltip().setText(cardInfo.getAccountUUID());

                super.open(owner, modal);
            } else {
                throw new Exception("Unrecognized card type!");
            }
        } catch (Exception exception) {
            MessageBox.show(getStage(), exception.getMessage(), "Get card info failure", MessageBox.ICON_ERROR | MessageBox.OK);
        }
    }

    @FXML
    private void onMoney() {
        open(withdrawController, true);

        hide();
    }

    @FXML
    private void onNotImplemented() {
        MessageBox.show(getStage(), "Not implemented!", "Failure", MessageBox.ICON_ERROR | MessageBox.OK);
    }

    @FXML
    private void close() {
        hide();
    }
}
