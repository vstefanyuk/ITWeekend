package org.quadrate.curiosity.raspy.network;

import org.quadrate.curiosity.raspy.Service;
import org.quadrate.curiosity.raspy.system.SystemService;

public interface NetworkService extends Service {
    void setSystemService(SystemService systemService);

    void setHost(String host);
    void setControlPort(int controlPort);

    void setName(String name);

    void setOperatorPassword(String operatorPassword);
    void setObserverPassword(String observerPassword);
}
