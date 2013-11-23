package com.softserve.itw2013.javafx.terminal;

import com.softserve.itw2013.javafx.terminal.data.Account;
import com.softserve.itw2013.javafx.terminal.data.CashTransaction;
import com.softserve.itw2013.javafx.terminal.data.Terminal;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 22.10.13
 * Time: 1:11
 * To change this template use File | Settings | File Templates.
 */
public interface TerminalDispatcher {
    Authentication authenticate(String login, String password);

    List<Account> getAllAccounts();

    List<Terminal> getAllTerminals();

    List<CashTransaction> getAllCashTransactions();

    BigDecimal getAccountBalance(String accountUuid);

    CashTransaction withdrawCash(String accountUuid, String terminalUuid, BigDecimal amount);
}
