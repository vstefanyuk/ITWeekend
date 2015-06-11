package org.quadrate.curiosity.raspy.serviceFactory;

import org.quadrate.curiosity.raspy.network.NetworkService;
import org.quadrate.curiosity.raspy.system.SystemService;

public interface ServiceFactory {
    SystemService getSystemService();

    NetworkService getNetworkService();
}
