package de.fhg.ivi.drm.it.dsc;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.support.TestPropertyProvider;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public interface DSCTest extends TestPropertyProvider {

    String SERVICE_NAME_DSC = "provider";
    int SERVICE_PORT_DSC = 8080;

    String user = "admin";
    String password = "password";
    String dscCredentials = "Basic " +
            Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));

    String idsDscAccessUrl = "http://" + SERVICE_NAME_DSC + ":" + SERVICE_PORT_DSC + "/api/ids/data";

    default <T> T getHttpResponseOrFail(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientResponseException e) {
            fail(e.getStatus() + " - " + e.getMessage() + " - " + e.getResponse().getBody(String.class).orElse(""));
        }
        throw new RuntimeException("Should fail");
    }

    default List<String> parseRegisteredConnectors(String queryResult) {
        return Stream
                .of(queryResult.trim().split("\\?url\\n<|>\\n<|>"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    default Map<String, String> getProperties() {
        return CollectionUtils.mapOf(
                "micronaut.http.services.dsc.url",
                "http://" + getDSCContainer().getHost() + ":" + getDSCContainer().getFirstMappedPort()
        );
    }

    GenericContainer<?> getDSCContainer();

}
