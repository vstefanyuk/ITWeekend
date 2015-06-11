package org.quadrate.curiosity.data.message;

public class CameraRequest extends AbstractMessage {
    private final String uuid;

    public CameraRequest(final String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
