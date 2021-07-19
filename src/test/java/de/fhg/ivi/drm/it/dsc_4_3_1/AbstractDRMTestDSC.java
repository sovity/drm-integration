package de.fhg.ivi.drm.it.dsc_4_3_1;

import de.fhg.ivi.drm.it.AbstractDRMTestBroker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Testcontainers
@Slf4j
public abstract class AbstractDRMTestDSC extends AbstractDRMTestBroker {

    public static final String CONTAINER_IMAGE_DSC = "ghcr.io/international-data-spaces-association/dataspace-connector:4.1.0";

    public static final String SERVICE_NAME_DSC = "provider";
    public static final int SERVICE_PORT_DSC = 8080;

    @Container
    static final GenericContainer<?> dscContainer = new GenericContainer<>(CONTAINER_IMAGE_DSC)
            .withExposedPorts(SERVICE_PORT_DSC)
            .withNetwork(network)
            .withNetworkAliases(SERVICE_NAME_DSC)
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("DSC"))
            .withEnv(
                    Map.of("SECURITY_REQUIRE-SSL", "false",
                            "SERVER_SSL_ENABLED", "false",
                            "CONFIGURATION_PATH", "/app/conf/config.json"))
            .withClasspathResourceMapping("config_provider.json","/app/conf/config.json",
                    BindMode.READ_ONLY)
//            .dependsOn(brokerContainer)
            .waitingFor(
                    Wait.forLogMessage(".*Started ConnectorApplication.*", 1)
            );

    public static final String user = "admin";
    public static final String password = "password";
    public static final String dscCredentials = "Basic " +
            Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

    static String urlDSC;
    static String idsDscAccessUrl;

    @BeforeAll
    public static void init() {
        urlDSC = "http://" + dscContainer.getHost() + ":" + dscContainer.getFirstMappedPort();
        idsDscAccessUrl = "http://" + SERVICE_NAME_DSC + ":" + SERVICE_PORT_DSC + "/api/ids/data";
    }

}
