package com.softserve.itw2013.javafx.terminal.admin.controller;

import com.softserve.itw2013.javafx.terminal.admin.data.LogonData;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.Validate;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 19.11.13
 * Time: 0:55
 * To change this template use File | Settings | File Templates.
 */
public class LogonController extends AbstractJFXController {
    public class LogonDataModel extends LogonData {
        private final StringProperty loginProperty = new SimpleStringProperty();
        private final StringProperty passwordProperty = new SimpleStringProperty();

        @Override
        public String getLogin() {
            return loginProperty.getValue();
        }

        @Override
        public void setLogin(String login) {
            loginProperty.setValue(login);
        }

        @Override
        public String getPassword() {
            return passwordProperty.getValue();
        }

        @Override
        public void setPassword(String password) {
            passwordProperty.setValue(password);
        }

        public StringProperty getLoginProperty() {
            return loginProperty;
        }

        public StringProperty getPasswordProperty() {
            return passwordProperty;
        }
    }

    @FXML
    private TextField txfLogin;
    @FXML
    private PasswordField pwfPassword;

    private final LogonDataModel model = new LogonDataModel();

    private LogonData data = new LogonData();

    private boolean accepted;

    public LogonController() {
        super(false);
    }

    public boolean open(final Stage owner, final LogonData data) {
        Validate.notNull(owner);
        Validate.notNull(data);

        this.data = data;

        try {
            BeanUtils.copyProperties(model, data);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        accepted = false;

        open(owner, true);

        return OK_RC.equals(getReturnCode());
    }

    @FXML
    private void initialize() {
        txfLogin.textProperty().bindBidirectional(model.getLoginProperty());
        pwfPassword.textProperty().bindBidirectional(model.getPasswordProperty());
    }

    @Override
    protected void configureStage(final Stage stage) {
        super.configureStage(stage);

        stage.setTitle("Logon");
    }

    @FXML
    private void onOK() {
        try {
            BeanUtils.copyProperties(data, model);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        setReturnCode(OK_RC);

        hide();
    }

    @FXML
    private void onCancel() {
        setReturnCode(CANCEL_RC);

        hide();
    }
}
