package de.fhg.ivi.drm.it.dsc;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
public interface DSC_7_0_3 extends DSCTest {
    String CONTAINER_IMAGE_DSC = "ghcr.io/international-data-spaces-association/dataspace-connector:7.0.3";

    @Container
    GenericContainer<?> dscContainer = new GenericContainer<>(CONTAINER_IMAGE_DSC)
            .withExposedPorts(8080)
            .withNetwork(Network.SHARED)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(DSC_7_0_3.class)).withPrefix("DSC"))
            .withEnv(
                    Map.of("SECURITY_REQUIRE-SSL", "false",
                            "SERVER_SSL_ENABLED", "false",
                            "CLEARING_HOUSE_URL", "",
                            "CONFIGURATION_PATH", "/app/conf/config.json",
                            "BOOTSTRAP_ENABLED", "true",
                            "BOOTSTRAP_PATH", "/app/bootstrap",
                    "LOG4J_CONFIGURATION_FILE", "/app/conf/log4j2.xml"))
            .withClasspathResourceMapping("config_provider_7_0_0.json", "/app/conf/config.json",
                    BindMode.READ_ONLY)
            .withClasspathResourceMapping("bootstrap.properties",
                    "/app/bootstrap/bootstrap.properties", BindMode.READ_ONLY)
            .withClasspathResourceMapping("catalog.jsonld",
                    "/app/bootstrap/catalog.jsonld", BindMode.READ_ONLY)
            .withClasspathResourceMapping("log4j2.xml", "/app/conf/log4j2.xml",
                    BindMode.READ_ONLY)
            .waitingFor(
                    Wait.forLogMessage(".*Started ConnectorApplication.*", 1)
            );

    default GenericContainer<?> getDSCContainer() {
        return dscContainer;
    }

}
