package org.quadrate.curiosity.data.message;

public class CameraResponse extends AbstractMessage {
    private final String uuid;
    private final Integer port;

    public CameraResponse(final String uuid, final Integer port) {
        this.uuid = uuid;
        this.port = port;
    }

    public String getUuid() {
        return uuid;
    }

    public Integer getPort() {
        return port;
    }
}
