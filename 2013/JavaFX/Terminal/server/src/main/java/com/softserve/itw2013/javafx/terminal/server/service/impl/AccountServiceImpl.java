package com.softserve.itw2013.javafx.terminal.server.service.impl;

import com.softserve.itw2013.javafx.terminal.data.Account;
import com.softserve.itw2013.javafx.terminal.exception.NotExistingEntityException;
import com.softserve.itw2013.javafx.terminal.server.repository.AccountRepository;
import com.softserve.itw2013.javafx.terminal.server.service.AccountService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
@Transactional(readOnly = true)
@Service("accountService")
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<Account> getAllAccounts() {
        return new ArrayList<Account>(accountRepository.findAll((Specification<Account>) null));
    }

    @Override
    public BigDecimal getAccountBalance(final String accountUuid) {
        Validate.notNull(accountUuid);

        final Account account = accountRepository.findOneByUuid(accountUuid);

        if (account == null) {
            throw new NotExistingEntityException(Account.class, "uuid", accountUuid);
        }

        return account.getBalance();
    }
}
