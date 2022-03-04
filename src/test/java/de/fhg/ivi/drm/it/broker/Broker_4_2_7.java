package de.fhg.ivi.drm.it.broker;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
public interface Broker_4_2_7 {

    String CONTAINER_IMAGE_BROKER_CORE = "registry.gitlab.cc-asp.fraunhofer.de/eis-ids/broker/core:4.2.7-SNAPSHOT";
    String CONTAINER_IMAGE_BROKER_FUSEKI = "app-store.ids.isst.fraunhofer.de:5000/ids/eis-broker-fuseki";
    String CONTAINER_IMAGE_BROKER_ELASTICSEARCH = "elasticsearch:7.9.3";

    String SERVICE_NAME_BROKER = "broker";
    String SERVICE_NAME_ELASTICSEARCH = "mobids-elasticsearch";

    int SERVICE_PORT_BROKER = 8080;

    String CONNECTOR_QUERY = "PREFIX ids: <https://w3id.org/idsa/core/> " +
            " SELECT ?url WHERE { ?x a ids:ConnectorEndpoint . " +
            " ?x ids:accessURL ?url }";

    String urlBroker = "http://" + SERVICE_NAME_BROKER + ":" + SERVICE_PORT_BROKER;

    @Container
    GenericContainer<?> brokerFusekiContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_FUSEKI)
            .withExposedPorts(3030)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("fuseki")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Broker_4_2_7.class)).withPrefix("BROKER_FUSEKI"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    GenericContainer<?> brokerElasticsearchContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_ELASTICSEARCH)
            .withExposedPorts(9200)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(SERVICE_NAME_ELASTICSEARCH)
            .withEnv(
                    Map.of("HTTP_PORT", "9200",
                            "http.cors.enabled", "false",
//                            "http.cors.allow-origin", "https://broker",
//                            "http.cors.allow-headers", "X-Requested-With,X-Auth-Token,Content-Type,Content-Length,Authorization",
//                            "http.cors.allow-credentials", "true",
                            "discovery.type", "single-node"))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Broker_4_2_7.class)).withPrefix("BROKER_ELASTIC"))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Container
    GenericContainer<?> brokerContainer = new GenericContainer<>(CONTAINER_IMAGE_BROKER_CORE)
            .withExposedPorts(SERVICE_PORT_BROKER)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(SERVICE_NAME_BROKER)
            .withEnv(
                    Map.of("SPARQL_ENDPOINT", "http://fuseki:3030/connectorData",
                            "ELASTICSEARCH_HOSTNAME", SERVICE_NAME_ELASTICSEARCH,
                            "SHACL_VALIDATION", "true",
                            "DAPS_VALIDATE_INCOMING", "false",
                            "COMPONENT_URI", "https://broker",
                            "COMPONENT_CATALOGURI", "https://broker/connectors/"))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("BrokerLog")).withPrefix(SERVICE_NAME_BROKER))
            .dependsOn(brokerElasticsearchContainer, brokerFusekiContainer)
            .waitingFor(
                    Wait.forListeningPort()
            );

}
