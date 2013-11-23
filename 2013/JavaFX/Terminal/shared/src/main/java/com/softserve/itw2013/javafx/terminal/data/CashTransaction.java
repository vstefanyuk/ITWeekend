package com.softserve.itw2013.javafx.terminal.data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "cash_transaction")
public class CashTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "cash_transaction_seq")
    @SequenceGenerator(name = "cash_transaction_seq", sequenceName = "account_seq")
    @Column(name = "cash_transaction_id")
    private Long id;

    @Column(name = "withdrawn")
    private Timestamp withdrawn;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "result_balance")
    private BigDecimal resultBalance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(Timestamp withdrawn) {
        this.withdrawn = withdrawn;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getResultBalance() {
        return resultBalance;
    }

    public void setResultBalance(BigDecimal resultBalance) {
        this.resultBalance = resultBalance;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }
}
