package org.quadrate.curiosity.raspy;

import java.io.Serializable;

public class RaspyConfig implements Serializable {
    private String name;
    private String operatorPassword;
    private String observerPassword;
    private String host;
    private int controlPort;
    private int videoPort;
    private int audioPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperatorPassword() {
        return operatorPassword;
    }

    public void setOperatorPassword(String operatorPassword) {
        this.operatorPassword = operatorPassword;
    }

    public String getObserverPassword() {
        return observerPassword;
    }

    public void setObserverPassword(String observerPassword) {
        this.observerPassword = observerPassword;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getControlPort() {
        return controlPort;
    }

    public void setControlPort(int controlPort) {
        this.controlPort = controlPort;
    }

    public int getVideoPort() {
        return videoPort;
    }

    public void setVideoPort(int videoPort) {
        this.videoPort = videoPort;
    }

    public int getAudioPort() {
        return audioPort;
    }

    public void setAudioPort(int audioPort) {
        this.audioPort = audioPort;
    }
}
