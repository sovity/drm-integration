/*
  Copyright 2022 Fraunhofer Institute for Transportation and Infrastructure Systems IVI

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

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
public interface DSC_6_3_1 extends DSCTest {
    String CONTAINER_IMAGE_DSC = "ghcr.io/international-data-spaces-association/dataspace-connector:6.3.1";

    @Container
    GenericContainer<?> dscContainer = new GenericContainer<>(CONTAINER_IMAGE_DSC)
            .withExposedPorts(8080)
            .withNetwork(Network.SHARED)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(DSC_6_3_1.class)).withPrefix("DSC"))
            .withEnv(
                    Map.of("SECURITY_REQUIRE-SSL", "false",
                            "SERVER_SSL_ENABLED", "false",
                            "CLEARING_HOUSE_URL", "",
                            "CONFIGURATION_PATH", "/app/conf/config.json",
                            "BOOTSTRAP_ENABLED", "true",
                            "HTTP_TIMEOUT_READ", "20000",
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

    default GenericContainer<?> getDSCContainer() {
        return dscContainer;
    }
}
