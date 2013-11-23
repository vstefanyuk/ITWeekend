package com.softserve.itw2013.javafx.terminal.admin.controller;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;
import com.softserve.itw2013.javafx.terminal.admin.TerminalAdminContext;
import com.softserve.itw2013.javafx.terminal.admin.data.LogonData;
import com.softserve.itw2013.javafx.terminal.controller.AbstractJFXController;
import com.softserve.itw2013.javafx.terminal.data.Account;
import com.softserve.itw2013.javafx.terminal.data.CashTransaction;
import com.softserve.itw2013.javafx.terminal.data.Terminal;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 07.11.13
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class MainController extends AbstractJFXController {
    @FXML
    private LogonController logonController;

    @FXML
    private MenuItem mniLogon;
    @FXML
    private MenuItem mniLogout;
    @FXML
    private MenuItem mniRefresh;

    @FXML
    private Button btnLogon;
    @FXML
    private Button btnRefresh;

    @FXML
    private TabPane tbpContent;

    @FXML
    private TableView<Terminal> tbvTerminals;

    @FXML
    private TableView<Account> tbvAccounts;

    @FXML
    private TableView<CashTransaction> tbvCashTransactions;
    @FXML
    private TableColumn<CashTransaction, Long> tbcCashTransactionsTerminal;
    @FXML
    private TableColumn<CashTransaction, Timestamp> tbcCashTransactionsAccount;

    private final ObservableList<Terminal> terminals = FXCollections.<Terminal>observableArrayList();
    private final ObservableList<Account> accounts = FXCollections.<Account>observableArrayList();
    private final ObservableList<CashTransaction> cashTransactions = FXCollections.<CashTransaction>observableArrayList();

    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private final LogonData logonData = new LogonData();

    public MainController() {
        super(false);
    }

    @FXML
    public void initialize() throws Exception {
        final BooleanBinding notConnected = connected.not();

        mniLogon.disableProperty().bind(connected);
        mniLogout.disableProperty().bind(notConnected);
        mniRefresh.disableProperty().bind(notConnected);

        btnLogon.disableProperty().bind(connected);
        btnRefresh.disableProperty().bind(notConnected);

        tbpContent.visibleProperty().bind(connected);

        tbvTerminals.setItems(terminals);

        tbvAccounts.setItems(accounts);

        tbvCashTransactions.setItems(cashTransactions);
    }

    @Override
    protected void configureStage(final Stage stage) {
        super.configureStage(stage);

        stage.setTitle("Terminal administration");
    }

    @FXML
    private void logon() {
        logonData.setPassword(null);

        if (logonController.open(getStage(), logonData)) {
            try {
                SecurityContextHolder.getContext().setAuthentication(TerminalAdminContext.getTerminalDispatcher().authenticate(logonData.getLogin(), logonData.getPassword()));

                connected.setValue(true);
            } catch (Exception exception) {
                MessageBox.show(getStage(), exception.getMessage(), "Logon failure", MessageBox.ICON_ERROR | MessageBox.OK);
            }

            refresh();
        }
    }

    @FXML
    private void logout() {
        terminals.clear();
        accounts.clear();
        cashTransactions.clear();

        connected.setValue(false);

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    @FXML
    private void refresh() {
        final TerminalDispatcher terminalDispatcher = TerminalAdminContext.getTerminalDispatcher();

        terminals.clear();
        accounts.clear();
        cashTransactions.clear();

        try {
            terminals.addAll(terminalDispatcher.getAllTerminals());
            accounts.addAll(terminalDispatcher.getAllAccounts());
            cashTransactions.addAll(terminalDispatcher.getAllCashTransactions());
        } catch (Exception exception) {
            MessageBox.show(getStage(), exception.getMessage(), "Refresh failure", MessageBox.ICON_ERROR | MessageBox.OK);
        }
    }

    @FXML
    private void about() {
        MessageBox.show(getStage(), "Terminal JavaFX sample\n\nDeveloped by V.Stefanyuk\n\nvstefanyuk@gmail.com", "About", MessageBox.ICON_INFORMATION | MessageBox.OK);
    }
}
