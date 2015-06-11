package org.quadrate.curiosity.raspy.system;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractDevice {
    protected static Lock I2C_LOCK = new ReentrantLock();
    protected static Lock SPI_LOCK = new ReentrantLock();
}
