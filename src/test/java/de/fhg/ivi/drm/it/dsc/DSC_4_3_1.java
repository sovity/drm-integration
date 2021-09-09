package de.fhg.ivi.drm.it.dsc;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Testcontainers
public interface DSC_4_3_1 {

    String CONTAINER_IMAGE_DSC = "ghcr.io/international-data-spaces-association/dataspace-connector:4.1.0";

    String SERVICE_NAME_DSC = "provider";
    int SERVICE_PORT_DSC = 8080;

    @Container
    GenericContainer<?> dscContainer = new GenericContainer<>(CONTAINER_IMAGE_DSC)
            .withExposedPorts(SERVICE_PORT_DSC)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(SERVICE_NAME_DSC)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(DSC_4_3_1.class)).withPrefix("DSC"))
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

    String user = "admin";
    String password = "password";
    String dscCredentials = "Basic " +
            Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

    String idsDscAccessUrl = "http://" + SERVICE_NAME_DSC + ":" + SERVICE_PORT_DSC + "/api/ids/data";

}
