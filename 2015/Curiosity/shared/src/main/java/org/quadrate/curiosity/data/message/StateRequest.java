package org.quadrate.curiosity.data.message;

public class StateRequest extends AbstractMessage {
    private final String uuid;

    public StateRequest(final String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
