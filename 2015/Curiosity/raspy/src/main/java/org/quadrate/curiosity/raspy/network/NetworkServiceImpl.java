package org.quadrate.curiosity.raspy.network;

import com.pi4j.util.StringUtil;
import org.quadrate.curiosity.data.message.AbstractMessage;
import org.quadrate.curiosity.data.message.CameraRequest;
import org.quadrate.curiosity.data.message.CameraResponse;
import org.quadrate.curiosity.data.message.MoveOn;
import org.quadrate.curiosity.data.message.StateRequest;
import org.quadrate.curiosity.data.message.StateResponse;
import org.quadrate.curiosity.data.message.TurnLights;
import org.quadrate.curiosity.raspy.AbstractBackgroundService;
import org.quadrate.curiosity.raspy.system.SystemService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkServiceImpl extends AbstractBackgroundService implements NetworkService {
    private SystemService systemService;

    private String host;
    private int controlPort;

    private String name;

    private String operatorPassword;
    private String observerPassword;

    private volatile ServerSocket serverSocket;

    public void setSystemService(SystemService systemService) {
        this.systemService = systemService;
    }

    @Override
    public void setHost(final String host) {
        this.host = host;
    }

    @Override
    public void setControlPort(final int controlPort) {
        this.controlPort = controlPort;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void setOperatorPassword(String operatorPassword) {
        this.operatorPassword = operatorPassword;
    }

    @Override
    public void setObserverPassword(String observerPassword) {
        this.observerPassword = observerPassword;
    }

    @Override
    protected void startImpl() throws Exception {
        final InetAddress inetAddress = !StringUtil.isNullOrEmpty(host) ? InetAddress.getByName(host) : null;

        serverSocket = new ServerSocket(controlPort, 0, inetAddress);

        super.startImpl();
    }

    @Override
    protected boolean isAliveImpl() {
        return super.isAliveImpl() && serverSocket != null && !serverSocket.isClosed();
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch(final IOException exception) {
                if (!isStopping()) {
                    LOGGER.error("Failure closing server socket", exception);
                }
            } finally {
                serverSocket = null;
            }
        }
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            final Socket socket;

            try {
                socket = serverSocket.accept();
            } catch (final IOException exception) {
                if (!isStopping()) {
                    LOGGER.error("Failure accepting client connection", exception);
                }

                break;
            }

            final Thread clientThread = new Thread(() -> {
                processClientConnection(socket);
            });

            clientThread.start();
        }
    }

    private void processClientConnection(final Socket socket) {
        try {
            final ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());

            final ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());

            while(!Thread.interrupted()) {
                final AbstractMessage requestMessage;

                try {
                    requestMessage = (AbstractMessage)clientInputStream.readObject();
                } catch (final IOException exception) {
                    if (!isStopping()) {
                        LOGGER.error("Failure reading message", exception);
                    }

                    break;
                }

                final AbstractMessage responseMessage = dispatchMessage(requestMessage);

                if (responseMessage != null) {
                    try {
                        clientOutputStream.writeObject(responseMessage);

                        clientOutputStream.flush();
                    } catch (final IOException exception) {
                        if (!isStopping()) {
                            LOGGER.error("Failure writing message", exception);
                        }

                        break;
                    }
                }
            }
        } catch (Exception exception) {
            if (!isStopping()) {
                LOGGER.error("Failure processing client connection", exception);
            }
        }
    }

    private AbstractMessage dispatchMessage(final AbstractMessage requestMessage) {
        AbstractMessage responseMessage = null;

        if (requestMessage instanceof TurnLights) {
            systemService.turnLights(((TurnLights)requestMessage).isOn());
        }

        if (requestMessage instanceof MoveOn) {
            final MoveOn moveOn = (MoveOn)requestMessage;

            systemService.moveOn(moveOn.getDirection(), moveOn.getIntensity());
        }

        if (requestMessage instanceof StateRequest) {
            final StateRequest stateRequest = (StateRequest)requestMessage;

            responseMessage = new StateResponse(stateRequest.getUuid(), systemService.getState());
        }

        if (requestMessage instanceof CameraRequest) {
            final CameraRequest cameraRequest = (CameraRequest)requestMessage;

            responseMessage = new CameraResponse(cameraRequest.getUuid(), systemService.runCamera());
        }

        return responseMessage;
    }
}
