package com.softserve.itw2013.javafx.terminal.client;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;
import com.softserve.itw2013.javafx.terminal.cardman.CardManager;
import com.softserve.itw2013.javafx.terminal.controller.JFXController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.10.13
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public class TerminalClientApp extends Application {
    private AbstractApplicationContext applicationContext;

    @Override
    public void init() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");

        TerminalClientContext.setTerminalDispatcher(applicationContext.getBean(TerminalDispatcher.class));
        TerminalClientContext.setCardManager(applicationContext.getBean(CardManager.class));

        TerminalClientContext.setTerminalUuid((String) applicationContext.getBean("terminalUuid"));
    }

    @Override
    public void start(final Stage stage) {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/cardIn.fxml"));

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        final JFXController controller = fxmlLoader.getController();

        controller.assignStage(stage);

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        TerminalClientContext.setCardManager(null);
        TerminalClientContext.setTerminalDispatcher(null);

        applicationContext.close();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
        /*Card card = TerminalFactory.getDefault().terminals().list(CardTerminals.State.CARD_PRESENT).get(0).connect("*");

        CardChannel channel = card.getBasicChannel();*/
        /*ResponseAPDU responseChangePin = channel.transmit(new CommandAPDU(0xFF, 0x21, 0x00, 0x00, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, 0x12, 0x34, 0x00}));*/

        /*ResponseAPDU responsePin = channel.transmit(new CommandAPDU(0xFF, 0x20, 0x00, 0x00, new byte[] {0x12, 0x34, 0x00}));*/

        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write((TerminalConstants.CARD_SIGNATURE_VALUE + TerminalConstants.CARD_FIELDS_DELIMITER).getBytes(TerminalConstants.CARD_DATA_ENCODING));

        byte[] cardInfo = (UUID.randomUUID().toString() + TerminalConstants.CARD_FIELDS_DELIMITER + "Katya Burla").getBytes(TerminalConstants.CARD_DATA_ENCODING);

        String cardInfoLength = Integer.toHexString(cardInfo.length);

        baos.write(((cardInfoLength.length() == 1?"0":"") + cardInfoLength + TerminalConstants.CARD_FIELDS_DELIMITER).getBytes(TerminalConstants.CARD_DATA_ENCODING));

        baos.write(cardInfo);

        byte[] data = baos.toByteArray();

        ResponseAPDU responseWrite = channel.transmit(new CommandAPDU(0xFF, 0xD6, 0x00, TerminalConstants.CARD_DATA_OFFSET, data));

        ResponseAPDU responseRead = channel.transmit(new CommandAPDU(0xFF, 0xB0, 0x00, TerminalConstants.CARD_DATA_OFFSET, data.length));

        System.out.println(new String(responseRead.getData(), TerminalConstants.CARD_DATA_ENCODING));*/
    }
}
