package org.quadrate.curiosity.raspy;

public abstract class AbstractBackgroundService extends AbstractService implements Runnable {
    private volatile Thread thread;

    @Override
    protected void startImpl() throws Exception {
        thread = new Thread(this, getClass().getCanonicalName());

        thread.setDaemon(false);

        thread.start();
    }

    @Override
    protected boolean isAliveImpl() {
        return thread != null && thread.isAlive();
    }

    @Override
    protected void stopImpl() {
        if (thread != null) {
            try {
                if (thread.isAlive()) {
                    thread.interrupt();

                    try {
                        thread.join(3000);
                    } catch (final InterruptedException exception) {
                    }
                }
            } finally {
                thread = null;
            }
        }
    }
}
