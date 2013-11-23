package com.softserve.itw2013.javafx.terminal.cardman;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 14.11.13
 * Time: 23:55
 * To change this template use File | Settings | File Templates.
 */
public class CardInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accountUUID;
    private final String ownerName;

    public CardInfo(final String accountUUID, final String ownerName) {
        Validate.notNull(accountUUID);
        Validate.notNull(ownerName);

        this.accountUUID = accountUUID;
        this.ownerName = ownerName;
    }

    public String getAccountUUID() {
        return accountUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
