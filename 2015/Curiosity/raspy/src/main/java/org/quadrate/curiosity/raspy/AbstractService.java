package org.quadrate.curiosity.raspy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractService implements Service {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private volatile boolean running;
    private volatile boolean stopping;

    @Override
    public final boolean isRunning() {
        return running;
    }

    @Override
    public final void start() throws Exception {
        writeLock.lock();

        try {
            if (running) {
                final String errMsg = String.format("Service '%s' is already running!", getClass().getCanonicalName());

                LOGGER.error(errMsg);

                throw new IllegalStateException(errMsg);
            }

            try {
                startImpl();

                running = true;
            } catch(final Exception exception) {
                stopImpl();

                throw exception;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public final boolean isAlive() {
        readLock.lock();

        try {
            if (!running) {
                return false;
            }

            return isAliveImpl();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public final boolean stop() {
        writeLock.lock();

        try {
            if (!running) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(String.format("Service '%s' is not running", getClass().getCanonicalName()));
                }

                return false;
            }

            stopping = true;

            try {
                stopImpl();
            } finally {
                stopping = false;

                running = false;
            }

            return true;
        } finally {
            writeLock.unlock();
        }
    }

    protected final boolean isStopping() {
        return stopping;
    }

    protected abstract void startImpl() throws Exception;

    protected abstract boolean isAliveImpl();

    protected abstract void stopImpl();
}
