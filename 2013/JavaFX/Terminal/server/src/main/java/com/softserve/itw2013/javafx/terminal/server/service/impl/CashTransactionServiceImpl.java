package com.softserve.itw2013.javafx.terminal.server.service.impl;

import com.softserve.itw2013.javafx.terminal.data.Account;
import com.softserve.itw2013.javafx.terminal.data.CashTransaction;
import com.softserve.itw2013.javafx.terminal.data.Terminal;
import com.softserve.itw2013.javafx.terminal.exception.NotExistingEntityException;
import com.softserve.itw2013.javafx.terminal.exception.TerminalException;
import com.softserve.itw2013.javafx.terminal.server.repository.AccountRepository;
import com.softserve.itw2013.javafx.terminal.server.repository.CashTransactionRepository;
import com.softserve.itw2013.javafx.terminal.server.repository.TerminalRepository;
import com.softserve.itw2013.javafx.terminal.server.service.CashTransactionService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
@Transactional(readOnly = true)
@Service("cardTransactionService")
public class CashTransactionServiceImpl implements CashTransactionService {
    @Autowired
    private CashTransactionRepository cashTransactionRepository;

    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public List<CashTransaction> getAllCashTransactions() {
        return new ArrayList<CashTransaction>(cashTransactionRepository.findAll((Specification<CashTransaction>) null));
    }

    @Transactional
    @Override
    public CashTransaction withdrawCash(final String accountUuid, final String terminalUuid, final BigDecimal amount) {
        Validate.notNull(accountUuid);
        Validate.notNull(terminalUuid);
        Validate.notNull(amount);
        Validate.isTrue(amount.compareTo(BigDecimal.ZERO) > 0);

        final Account account = accountRepository.findOneByUuid(accountUuid);

        if (account == null) {
            throw new NotExistingEntityException(Account.class, "uuid", accountUuid);
        }

        EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory).refresh(account, LockModeType.PESSIMISTIC_WRITE);

        final Terminal terminal = terminalRepository.findOneByUuid(terminalUuid);

        if (terminal == null) {
            throw new NotExistingEntityException(Terminal.class, "uuid", terminalUuid);
        }

        final BigDecimal accountBalance = account.getBalance();

        final BigDecimal resultBalance = accountBalance.subtract(amount);

        if (resultBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new TerminalException("Not enough money");
        }

        account.setBalance(resultBalance);

        CashTransaction cashTransaction = new CashTransaction();

        cashTransaction.setAccount(account);
        cashTransaction.setTerminal(terminal);

        cashTransaction.setWithdrawn(new Timestamp(System.currentTimeMillis()));
        cashTransaction.setAmount(amount);
        cashTransaction.setResultBalance(resultBalance);

        cashTransaction = cashTransactionRepository.save(cashTransaction);

        return cashTransaction;
    }
}
