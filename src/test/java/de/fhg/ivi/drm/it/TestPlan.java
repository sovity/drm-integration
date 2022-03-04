package de.fhg.ivi.drm.it;

public interface TestPlan {

    void registerProviderAtBroker();

    void queryConnectorsFromBroker();

    void registerResourceAtBroker();

    void unregisterConnectorFromBroker();

}
