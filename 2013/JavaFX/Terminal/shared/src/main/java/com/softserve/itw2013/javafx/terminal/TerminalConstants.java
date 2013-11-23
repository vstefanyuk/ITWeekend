package com.softserve.itw2013.javafx.terminal;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 13.11.13
 * Time: 1:09
 * To change this template use File | Settings | File Templates.
 */
public interface TerminalConstants {
    String CARD_DATA_ENCODING = "US-ASCII";

    String CARD_SIGNATURE_VALUE = "TERMINAL";

    int CARD_SIGNATURE_LENGTH = CARD_SIGNATURE_VALUE.length();

    String CARD_FIELDS_DELIMITER = "$";

    int CARD_DATA_OFFSET = 0x20;

    int CARD_SIGNATURE_OFFSET = CARD_DATA_OFFSET;

    int CARD_INFO_SIZE_OFFSET = CARD_SIGNATURE_OFFSET + CARD_SIGNATURE_LENGTH + 1;

    int CARD_INFO_SIZE_LENGTH = 2;

    int CARD_INFO_OFFSET = CARD_INFO_SIZE_OFFSET + CARD_INFO_SIZE_LENGTH + 1;
}
