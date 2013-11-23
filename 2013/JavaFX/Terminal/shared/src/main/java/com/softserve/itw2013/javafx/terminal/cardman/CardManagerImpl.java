package com.softserve.itw2013.javafx.terminal.cardman;

import com.softserve.itw2013.javafx.terminal.TerminalConstants;
import com.softserve.itw2013.javafx.terminal.exception.TerminalException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.smartcardio.*;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 15.11.13
 * Time: 0:04
 * To change this template use File | Settings | File Templates.
 */
public class CardManagerImpl implements CardManager {
    private final Map<Integer, Card> cards = new ConcurrentHashMap<Integer, Card>();

    private final String cardTerminalName;

    public CardManagerImpl() {
        this(null);
    }

    public CardManagerImpl(final String cardTerminalName) {
        this.cardTerminalName = cardTerminalName;
    }

    @Override
    public Integer waitForCard() throws CardException {
        Card card;

        do {
            final CardTerminals terminals = TerminalFactory.getDefault().terminals();

            final List<CardTerminal> cardPresentTerminals;

            if (StringUtils.isEmpty(cardTerminalName)) {
                cardPresentTerminals = terminals.list(CardTerminals.State.CARD_PRESENT);
            } else {
                final CardTerminal cardTerminal = terminals.getTerminal(cardTerminalName);

                if (cardTerminal == null) {
                    throw new TerminalException(String.format("Card terminal with name '%s' is absent", cardTerminalName));
                }

                cardTerminal.waitForCardPresent(0);

                cardPresentTerminals = Collections.singletonList(cardTerminal);
            }

            if (!cardPresentTerminals.isEmpty()) {
                try {
                    card = cardPresentTerminals.get(0).connect("*");
                } catch (final CardNotPresentException exception) {
                    continue;
                } catch (final CardException exception) {
                    card = null;
                }

                break;
            }

            terminals.waitForChange();
        } while (true);

        Integer cardId = null;

        if (card != null) {
            try {
                final CardChannel channel = card.getBasicChannel();

                final ResponseAPDU responseAPDU = channel.transmit(new CommandAPDU(0xFF, 0xB0, 0x00, TerminalConstants.CARD_SIGNATURE_OFFSET, TerminalConstants.CARD_SIGNATURE_LENGTH));

                if (responseAPDU.getSW() == 0x9000) {
                    try {
                        if (TerminalConstants.CARD_SIGNATURE_VALUE.equals(new String(responseAPDU.getData(), TerminalConstants.CARD_DATA_ENCODING))) {
                            cardId = System.identityHashCode(card);
                        }
                    } catch (UnsupportedEncodingException exception) {
                    }

                    if (cardId != null) {
                        cards.put(cardId, card);
                    }
                }
            } finally {
                if (cardId == null) {
                    card.disconnect(false);
                }
            }
        }

        return cardId;
    }

    @Override
    public Integer verifyPinCode(final Integer cardId, final String pinCode) throws CardException {
        Validate.notNull(cardId);
        Validate.notNull(pinCode);

        final Card card = getCard(cardId);

        final byte[] pinCodeData = new byte[]{
                (byte) ((((pinCode.charAt(0) - '0') << 4) & 0xFF) | ((pinCode.charAt(1) - '0') & 0xFF)),
                (byte) ((((pinCode.charAt(2) - '0') << 4) & 0xFF) | ((pinCode.charAt(3) - '0') & 0xFF)),
                0x00
        };

        final ResponseAPDU responseAPDU = card.getBasicChannel().transmit(new CommandAPDU(0xFF, 0x20, 0x00, 0x00, pinCodeData));

        final int sw = responseAPDU.getSW();

        final Integer attemptsLeft;

        if (sw == 0x9000) {
            attemptsLeft = null;
        } else {
            if ((sw & 0xFFFC) == 0x63C0) {
                attemptsLeft = sw & 0x0003;
            } else {
                attemptsLeft = -1;
            }
        }

        return attemptsLeft;
    }

    @Override
    public CardInfo getCardInfo(final Integer cardId) throws CardException {
        Validate.notNull(cardId);

        final Card card = getCard(cardId);

        final CardChannel channel = card.getBasicChannel();

        ResponseAPDU responseAPDU = channel.transmit(new CommandAPDU(0xFF, 0xB0, 0x00, TerminalConstants.CARD_INFO_SIZE_OFFSET, TerminalConstants.CARD_INFO_SIZE_LENGTH));

        CardInfo cardInfo = null;

        if (responseAPDU.getSW() == 0x9000) {
            try {
                final int dataSize = Integer.parseInt(new String(responseAPDU.getData(), TerminalConstants.CARD_DATA_ENCODING), 0x10);

                responseAPDU = channel.transmit(new CommandAPDU(0xFF, 0xB0, 0x00, TerminalConstants.CARD_INFO_OFFSET, dataSize));

                if (responseAPDU.getSW() == 0x9000) {
                    final String[] data = new String(responseAPDU.getData(), TerminalConstants.CARD_DATA_ENCODING).split("\\" + TerminalConstants.CARD_FIELDS_DELIMITER);

                    cardInfo = new CardInfo(data[0], data[1]);
                }
            } catch (UnsupportedEncodingException exception) {
            }
        }

        return cardInfo;
    }

    private Card getCard(final Integer cardId) {
        Validate.notNull(cardId);

        final Card card = cards.get(cardId);

        if (card == null) {
            throw new IllegalStateException(String.format("Card with id %d is not found!", cardId));
        }

        return card;
    }
}
