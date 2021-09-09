package de.fhg.ivi.drm.it;

import de.fhg.ivi.drm.it.broker.Broker_4_2_3;
import de.fhg.ivi.drm.it.dsc.DSC_6_2_0;
import de.fhg.ivi.ids.dsc_5_1.api.IdsMessagesApiClient;
import de.fhg.ivi.ids.dsc_5_1.api.OfferedResourcesApiClient;
import de.fhg.ivi.ids.dsc_5_1.model.Link;
import de.fhg.ivi.ids.dsc_5_1.model.OfferedResourceView;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DRM Test - DSC 6.2.0 - Broker 4.2.3-SNAPSHOT")
public class DRMv3Test implements Broker_4_2_3, DSC_6_2_0, TestPlan, TestPropertyProvider {

    @Inject
    IdsMessagesApiClient messagesApiClient;

    @Inject
    OfferedResourcesApiClient offeredResourcesApiClient;

    @Test
    @Order(1)
    @Override
    public void registerProviderAtBroker() {
        var response = getHttpResponseOrFail(
                () -> messagesApiClient.sendConnectorUpdateMessage3(dscCredentials, urlBroker + "/infrastructure")
        );

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    @Order(2)
    @Override
    public void queryConnectorsFromBroker() {
        HttpResponse<String> response = getHttpResponseOrFail(
                () -> messagesApiClient.sendConnectorUpdateMessage2(dscCredentials, URI.create(urlBroker + "/infrastructure"), CONNECTOR_QUERY)
        );

        // then
        String result = assertDoesNotThrow(() -> response.getBody().orElseThrow());
        List<String> registeredConnectors = parseRegisteredConnectors(result);

        assertEquals(1, registeredConnectors.size());

        assertEquals(idsDscAccessUrl, registeredConnectors.get(0));
    }

    @Test
    @Order(3)
    public void registerResourceAtBroker() {
        var view = offeredResourcesApiClient.getAll3(dscCredentials, Optional.empty(), Optional.empty());
        var offeredResourceView = assertDoesNotThrow(() -> view.getBody().orElseThrow());

        offeredResourceView.getEmbedded().getResources().stream()
                .map(OfferedResourceView::getLinks)
                .map(links -> links.get("self"))
                .map(Link::getHref)
                .forEach(id -> {
                    var response = getHttpResponseOrFail(
                            () -> messagesApiClient.sendConnectorUpdateMessage(dscCredentials, urlBroker + "/infrastructure", URI.create(id))
                    );
                    assertEquals(HttpStatus.OK, response.getStatus());
                });
    }

    private <T> T getHttpResponseOrFail(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientResponseException e) {
            fail(e.getStatus() + " - " + e.getMessage() + " - " + e.getResponse().getBody(String.class).orElse(""));
        }
        throw new RuntimeException("Should fail");
    }

    private List<String> parseRegisteredConnectors(String queryResult) {
        return Stream
                .of(queryResult.trim().split("\\?url\\n<|>\\n<|>"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public Map<String, String> getProperties() {
        return CollectionUtils.mapOf(
                "micronaut.http.services.dsc.url", "http://" + dscContainer.getHost() + ":" + dscContainer.getFirstMappedPort()
        );
    }

}