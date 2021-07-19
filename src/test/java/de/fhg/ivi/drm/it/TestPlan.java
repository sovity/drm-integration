package de.fhg.ivi.drm.it;

import java.io.IOException;

public interface TestPlan {

    void registerProviderAtBroker() throws IOException;

    void queryConnectorsFromBroker() throws IOException;

}
