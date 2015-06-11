package org.quadrate.curiosity.raspy.system;

import org.quadrate.curiosity.State;
import org.quadrate.curiosity.raspy.Service;

public interface SystemService extends Service {
    void setVideoPort(int videoPort);

    void setAudioPort(int audioPort);

    State getState();

    void turnLights(boolean on);

    void moveOn(double direction, double intensity);

    Integer runCamera();
}
