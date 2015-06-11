package org.quadrate.curiosity.raspy;

import org.quadrate.curiosity.raspy.network.NetworkService;
import org.quadrate.curiosity.raspy.serviceFactory.ServiceFactory;
import org.quadrate.curiosity.raspy.system.SystemService;
import java.util.Objects;

public class RaspyServer extends AbstractService {
    private final SystemService systemService;
    private final NetworkService networkService;

    public RaspyServer(final RaspyConfig config, final ServiceFactory serviceFactory) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(serviceFactory);

        systemService = serviceFactory.getSystemService();
        systemService.setVideoPort(config.getVideoPort());
        systemService.setAudioPort(config.getAudioPort());

        networkService = serviceFactory.getNetworkService();
        networkService.setSystemService(systemService);
        networkService.setHost(config.getHost());
        networkService.setControlPort(config.getControlPort());
        networkService.setName(config.getName());
        networkService.setOperatorPassword(config.getOperatorPassword());
        networkService.setObserverPassword(config.getObserverPassword());
    }

    @Override
    protected void startImpl() throws Exception {
        systemService.start();

        networkService.start();
    }

    @Override
    protected boolean isAliveImpl() {
        return false;
    }

    @Override
    protected void stopImpl() {
        if (networkService.isRunning()) {
            networkService.stop();
        }

        if (systemService.isRunning()) {
            systemService.stop();
        }
    }
}
