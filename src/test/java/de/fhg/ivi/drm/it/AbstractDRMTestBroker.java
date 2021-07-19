package de.fhg.ivi.drm.it;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
@Slf4j
public abstract class AbstractDRMTestBroker {

    public static final String CONTAINER_IMAGE_BROKER_CORE = "app-store.ids.isst.fraunhofer.de:5000/ids/mobids-broker:4.0.3.0";
    public static final String CONTAINER_IMAGE_BROKER_FUSEKI = "app-store.ids.isst.fraunhofer.de:5000/ids/eis-broker-fuseki";
    public static final String CONTAINER_IMAGE_BROKER_ELASTICSEARCH = "elasticsearch:7.9.3";

    static final String SERVICE_NAME_BROKER = "broker";
    static final int SERVICE_PORT_BROKER = 8080;

    public static final String CONNECTOR_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/> " +
            " SELECT ?url WHERE { ?x a ids:ConnectorEndpoint . " +
            " ?x ids:accessURL ?url }";

    public static final String urlBroker = "http://" + SERVICE_NAME_BROKER + ":" + SERVICE_PORT_BROKER;

    public static Network network = Network.newNetwork();

    @Container
    public static final GenericContainer<?> brokerFusekiContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_FUSEKI)
            .withExposedPorts(3030)
            .withNetwork(network)
            .withNetworkAliases("fuseki")
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("BROKER_FUSEKI"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    public static final GenericContainer<?> brokerElasticsearchContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_ELASTICSEARCH)
            .withExposedPorts(9200)
            .withNetwork(network)
            .withNetworkAliases("broker-elasticsearch")
            .withEnv(
                    Map.of("http.port", "9200",
                            "http.cors.enabled", "true",
                            "http.cors.allow-origin", "https://localhost",
                            "http.cors.allow-headers", "X-Requested-With,X-Auth-Token,Content-Type,Content-Length,Authorization",
                            "http.cors.allow-credentials", "true",
                            "discovery.type", "single-node"))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("BROKER_ELASTIC"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    public static final GenericContainer<?> brokerContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_CORE)
            .withExposedPorts(SERVICE_PORT_BROKER)
            .withNetwork(network)
            .withNetworkAliases(SERVICE_NAME_BROKER)
            .withEnv(
                    Map.of("SPARQL_ENDPOINT", "http://fuseki:3030/connectorData",
                            "ELASTICSEARCH_HOSTNAME", "broker-elasticsearch",
                            "SHACL_VALIDATION", "true",
                            "DAPS_VALIDATE_INCOMING", "false",
                            "COMPONENT_URI", "https://localhost",
                            "COMPONENT_CATALOGURI", "https://localhost/connectors/"))
            .withLogConsumer(new Slf4jLogConsumer(log).withPrefix(SERVICE_NAME_BROKER))
            .dependsOn(brokerElasticsearchContainer, brokerFusekiContainer)
            .waitingFor(
                    Wait.forListeningPort()
            );

}
