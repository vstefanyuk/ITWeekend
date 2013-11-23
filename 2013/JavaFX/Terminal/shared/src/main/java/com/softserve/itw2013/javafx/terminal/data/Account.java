package com.softserve.itw2013.javafx.terminal.data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "account")
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_seq")
    @Column(name = "account_id")
    private Integer id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "balance")
    private BigDecimal balance;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
