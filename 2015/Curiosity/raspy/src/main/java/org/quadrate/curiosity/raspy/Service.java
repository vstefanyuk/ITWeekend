package org.quadrate.curiosity.raspy;

public interface Service {
    boolean isRunning();

    void start() throws Exception;

    boolean isAlive();

    boolean stop();
}
