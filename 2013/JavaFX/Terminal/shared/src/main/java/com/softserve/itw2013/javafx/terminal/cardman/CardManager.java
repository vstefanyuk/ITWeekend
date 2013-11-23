package com.softserve.itw2013.javafx.terminal.cardman;

import javax.smartcardio.CardException;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 14.11.13
 * Time: 23:43
 * To change this template use File | Settings | File Templates.
 */
public interface CardManager {
    /**
     * Waits for the card inserting.
     * Returns card id or <code>null</code> in case if card is unrecognized or incorrect.
     *
     * @return
     * @throws CardException
     */
    Integer waitForCard() throws CardException;

    /**
     * Verifies pin-code of the card with specified id.
     * Returns <code>null</code> in case if specified pin code is correct, amount of attempts left to check pin-code in case if specified pin-code is not correct and
     * <code>-1</code> in case if card is unsupported.
     *
     * @param cardId
     * @param pinCode
     * @return
     * @throws CardException
     */
    Integer verifyPinCode(Integer cardId, String pinCode) throws CardException;

    /**
     * Returns read card-info. In case of unsupported card <code>null</code> is returned.
     *
     * @param cardId
     * @return
     * @throws CardException
     */
    CardInfo getCardInfo(Integer cardId) throws CardException;
}
