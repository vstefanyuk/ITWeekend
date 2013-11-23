package com.softserve.itw2013.javafx.terminal.client;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;
import com.softserve.itw2013.javafx.terminal.cardman.CardInfo;
import com.softserve.itw2013.javafx.terminal.cardman.CardManager;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 16.11.13
 * Time: 0:07
 * To change this template use File | Settings | File Templates.
 */
public class TerminalClientContext {
    private static TerminalDispatcher terminalDispatcher;
    private static CardManager cardManager;

    private static String terminalUuid;

    private static Integer cardId;
    private static CardInfo cardInfo;

    public static TerminalDispatcher getTerminalDispatcher() {
        return terminalDispatcher;
    }

    static void setTerminalDispatcher(TerminalDispatcher terminalDispatcher) {
        TerminalClientContext.terminalDispatcher = terminalDispatcher;
    }

    public static CardManager getCardManager() {
        return cardManager;
    }

    static void setCardManager(CardManager cardManager) {
        TerminalClientContext.cardManager = cardManager;
    }

    public static String getTerminalUuid() {
        return terminalUuid;
    }

    static void setTerminalUuid(String terminalUuid) {
        TerminalClientContext.terminalUuid = terminalUuid;
    }

    public static Integer getCardId() {
        return cardId;
    }

    public static void setCardId(Integer cardId) {
        TerminalClientContext.cardId = cardId;
    }

    public static CardInfo getCardInfo() {
        return cardInfo;
    }

    public static void setCardInfo(CardInfo cardInfo) {
        TerminalClientContext.cardInfo = cardInfo;
    }
}
