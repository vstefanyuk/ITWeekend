package com.softserve.itw2013.javafx.terminal.client.controller;

import com.softserve.itw2013.javafx.terminal.client.TerminalClientContext;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import jfx.messagebox.MessageBox;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 07.11.13
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class LogonController extends AbstractJFXController {
    @FXML
    private HomeController homeController;

    @FXML
    private PasswordField pwfPinCode;
    @FXML
    private Button btnOK;
    @FXML
    private Button btn1;
    @FXML
    private Button btn2;
    @FXML
    private Button btn3;
    @FXML
    private Button btn4;
    @FXML
    private Button btn5;
    @FXML
    private Button btn6;
    @FXML
    private Button btn7;
    @FXML
    private Button btn8;
    @FXML
    private Button btn9;
    @FXML
    private Button btn0;
    @FXML
    private Button btnClear;

    private final StringProperty pinCodeProperty = new SimpleStringProperty();

    public LogonController() {
        super(true);
    }

    @FXML
    public void initialize() {
        pinCodeProperty.bindBidirectional(pwfPinCode.textProperty());

        final BooleanProperty okDisable = btnOK.disableProperty();

        pinCodeProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                final int pinCodeLength = newValue.length();

                okDisable.set(pinCodeLength != 4);

                if(!okDisable.get()) {
                    btnOK.requestFocus();
                }

                btnClear.setDisable(pinCodeLength == 0);
            }
        });

        final BooleanBinding okEnable = okDisable.not();

        btn1.disableProperty().bind(okEnable);
        btn2.disableProperty().bind(okEnable);
        btn3.disableProperty().bind(okEnable);
        btn4.disableProperty().bind(okEnable);
        btn5.disableProperty().bind(okEnable);
        btn6.disableProperty().bind(okEnable);
        btn7.disableProperty().bind(okEnable);
        btn8.disableProperty().bind(okEnable);
        btn9.disableProperty().bind(okEnable);
        btn0.disableProperty().bind(okEnable);
    }

    @FXML
    private void onNumber(final ActionEvent event) {
        pinCodeProperty.setValue(pinCodeProperty.getValue() + ((Button) event.getSource()).getText());
    }

    @FXML
    private void onOK() {
        try {
            final String pinCode = pinCodeProperty.getValue();

            pinCodeProperty.setValue("");

            final Integer attemptsLeft = TerminalClientContext.getCardManager().verifyPinCode(TerminalClientContext.getCardId(), pinCode);

            if (attemptsLeft == null) {
                open(homeController, true);

                hide();
            } else {
                if (attemptsLeft != -1) {
                    throw new Exception(String.format("Incorrect pin-code is entered!\n%s", attemptsLeft != 0 ? String.format("You have %s attempts left. Be careful!", attemptsLeft) : "You have no attempts left. Card is locked!"));
                } else {
                    throw new Exception("Unrecognized card type!");
                }
            }
        } catch (final Exception exception) {
            MessageBox.show(getStage(), exception.getMessage(), "Card pin-code verify failure", MessageBox.ICON_ERROR | MessageBox.OK);
        }
    }

    @FXML
    private void onClear() {
        pwfPinCode.clear();
    }

    @FXML
    private void onEscape() {
        hide();
    }
}
