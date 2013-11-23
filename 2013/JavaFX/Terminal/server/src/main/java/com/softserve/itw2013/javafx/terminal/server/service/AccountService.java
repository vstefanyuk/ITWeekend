package com.softserve.itw2013.javafx.terminal.server.service;

import com.softserve.itw2013.javafx.terminal.data.Account;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public interface AccountService {
    List<Account> getAllAccounts();

    BigDecimal getAccountBalance(String accountUuid);
}
