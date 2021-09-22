package de.fhg.ivi.drm.it;

import de.fhg.ivi.drm.it.broker.Broker_4_0_3;
import de.fhg.ivi.drm.it.dsc.DSC_5_1_2;
import de.fhg.ivi.drm.it.infomodel.MultipartParser;
import de.fhg.ivi.ids.dsc_5_1.api.IdsMessagesApiClient;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DRM Test - DSC 5.1.2")
@Disabled
public class DSC_5_Test implements Broker_4_0_3, DSC_5_1_2, TestPlan, TestPropertyProvider {

    @Inject
    IdsMessagesApiClient messagesApiClient;

    @Inject
    private Serializer serializer;

    @Test
    @Order(1)
    @Override
    public void registerProviderAtBroker() {
        HttpResponse<String> response = null;
        try {
            response = messagesApiClient.sendConnectorUpdateMessage3(dscCredentials, urlBroker + "/infrastructure");
        } catch (HttpClientResponseException e) {
            fail(e.getStatus() + " - " + e.getMessage() + " - " + e.getResponse().getBody(String.class).orElse(""));
        }

        // then
        String result = response.body();
        Map<String, String> parts = assertDoesNotThrow(() -> MultipartParser.stringToMultipart(result));
        Message message = assertDoesNotThrow(() -> convertToIdsMessage(parts.get("header")));
        assertTrue(
                message instanceof MessageProcessedNotificationMessage,
                () -> "Message is of unexpected type " + message.getClass() + " Messgage:\n" + message.toRdf()
        );
    }

    @Test
    @Order(2)
    @Override
    public void queryConnectorsFromBroker() {
        HttpResponse<String> response = null;
        try {
            response = messagesApiClient.sendConnectorUpdateMessage2(dscCredentials, URI.create(urlBroker + "/infrastructure"), CONNECTOR_QUERY);
        } catch (HttpClientResponseException e) {
            fail(e.getStatus() + " - " + e.getMessage() + " - " + e.getResponse().getBody(String.class).orElse(""));
        }

        // then
        String result = response.body();
        Map<String, String> parts = assertDoesNotThrow(() -> MultipartParser.stringToMultipart(result));
        List<String> registeredConnectors = parseRegisteredConnectors(parts.get("payload"));
        assertEquals(1, registeredConnectors.size());
        assertEquals(idsDscAccessUrl, registeredConnectors.get(0));
    }

    private Message convertToIdsMessage(String payload) throws IOException {
        return convertToIds(payload, Message.class);
    }

    private <T> T convertToIds(String payload, Class<T> clazz) throws IOException {
        return serializer.deserialize(payload, clazz);
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