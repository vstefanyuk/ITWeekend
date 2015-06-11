package org.quadrate.curiosity.raspy.serviceFactory;

import org.quadrate.curiosity.raspy.network.NetworkService;
import org.quadrate.curiosity.raspy.network.NetworkServiceImpl;
import org.quadrate.curiosity.raspy.system.SystemService;
import org.quadrate.curiosity.raspy.system.SystemServiceImpl;

public class DefaultServiceFactory extends AbstractServiceFactory{
    @Override
    protected SystemService createSystemService() {
        return new SystemServiceImpl();
    }

    @Override
    protected NetworkService createNetworkService() {
        return new NetworkServiceImpl();
    }
}
