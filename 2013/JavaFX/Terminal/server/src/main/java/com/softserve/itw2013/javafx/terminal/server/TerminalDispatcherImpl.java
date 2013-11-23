package com.softserve.itw2013.javafx.terminal.server;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;
import com.softserve.itw2013.javafx.terminal.data.Account;
import com.softserve.itw2013.javafx.terminal.data.CashTransaction;
import com.softserve.itw2013.javafx.terminal.data.Terminal;
import com.softserve.itw2013.javafx.terminal.server.service.AccountService;
import com.softserve.itw2013.javafx.terminal.server.service.CashTransactionService;
import com.softserve.itw2013.javafx.terminal.server.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 22.10.13
 * Time: 1:17
 * To change this template use File | Settings | File Templates.
 */
@Controller("terminalDispatcher")
public class TerminalDispatcherImpl implements TerminalDispatcher {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TerminalService terminalService;
    @Autowired
    private CashTransactionService cashTransactionService;

    @Override
    public Authentication authenticate(String login, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
    }

    @Secured("ROLE_ADMIN")
    @Override
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @Secured("ROLE_ADMIN")
    @Override
    public List<Terminal> getAllTerminals() {
        return terminalService.getAllTerminals();
    }

    @Secured("ROLE_ADMIN")
    @Override
    public List<CashTransaction> getAllCashTransactions() {
        return cashTransactionService.getAllCashTransactions();
    }

    @Secured("ROLE_TERMINAL")
    @Override
    public BigDecimal getAccountBalance(final String accountUuid) {
        return accountService.getAccountBalance(accountUuid);
    }

    @Secured("ROLE_TERMINAL")
    @Override
    public CashTransaction withdrawCash(final String accountUuid, final String terminalUuid, final BigDecimal amount) {
        return cashTransactionService.withdrawCash(accountUuid, terminalUuid, amount);
    }
}
