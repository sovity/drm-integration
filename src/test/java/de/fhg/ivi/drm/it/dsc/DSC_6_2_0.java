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
public interface DSC_6_2_0 {
    String CONTAINER_IMAGE_DSC = "ghcr.io/international-data-spaces-association/dataspace-connector:6.2.0";

    String SERVICE_NAME_DSC = "provider";
    int SERVICE_PORT_DSC = 8080;

    @Container
    GenericContainer<?> dscContainer = new GenericContainer<>(CONTAINER_IMAGE_DSC)
            .withExposedPorts(8080)
            .withNetwork(Network.SHARED)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(DSC_6_2_0.class)).withPrefix("DSC"))
            .withEnv(
                    Map.of("SECURITY_REQUIRE-SSL", "false",
                            "SERVER_SSL_ENABLED", "false",
                            "CLEARING_HOUSE_URL", "",
                            "CONFIGURATION_PATH", "/app/conf/config.json",
                            "BOOTSTRAP_ENABLED", "true",
                            "BOOTSTRAP_PATH", "/app/bootstrap"))
            .withClasspathResourceMapping("config_provider.json", "/app/conf/config.json",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("bootstrap.properties",
                    "/app/bootstrap/bootstrap.properties", BindMode.READ_ONLY)
            .withClasspathResourceMapping("catalog.jsonld",
                    "/app/bootstrap/catalog.jsonld", BindMode.READ_ONLY)
            .waitingFor(
                    Wait.forLogMessage(".*Started ConnectorApplication.*", 1)
            );

    String user = "admin";
    String password = "password";
    String dscCredentials = "Basic " +
            Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

    String idsDscAccessUrl = "http://" + SERVICE_NAME_DSC + ":" + SERVICE_PORT_DSC + "/api/ids/data";

}
