package com.softserve.itw2013.javafx.terminal.exception;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 18.11.13
 * Time: 0:12
 * To change this template use File | Settings | File Templates.
 */
public class NotExistingEntityException extends TerminalException {
    private final Class<? extends Serializable> entityType;
    private final String identityName;
    private final Serializable identityValue;

    public NotExistingEntityException(final Class<? extends Serializable> entityType, final Serializable entityId) {
        this(entityType, "id", entityId);
    }

    public NotExistingEntityException(final Class<? extends Serializable> entityType, final String identityName, final Serializable identityValue) {
        super(String.format("Entity '%s' with %s '%s' does not exist", entityType.getSimpleName(), identityName, identityValue));

        this.entityType = entityType;
        this.identityName = identityName;
        this.identityValue = identityValue;
    }

    public Class<? extends Serializable> getEntityType() {
        return entityType;
    }

    public String getIdentityName() {
        return identityName;
    }

    public Serializable getIdentityValue() {
        return identityValue;
    }
}
