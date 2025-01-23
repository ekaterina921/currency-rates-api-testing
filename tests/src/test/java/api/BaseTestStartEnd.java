package api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers(parallel = true)
public class BaseTestStartEnd {
    private final static Network mainNetwork = Network.newNetwork();
    private final static int APP_PORT = 8080;
    private final static int MAIN_DB_PORT = 27017;
    private final static int LOGS_DB_PORT = 27037;
    static String appHost;
    static String mainDBHost;
    static String logsDBHost;
    static int appPort1;

    static GenericContainer<?> appContainer = null;

    @Container
    final static GenericContainer<?> mongoLogContainer = new GenericContainer<>("mongo:latest")
            .withNetwork(mainNetwork)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "MainUser")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "Test123!")
            .withEnv("MONGO_INITDB_DATABASE", "Logging")
            .withExposedPorts(MAIN_DB_PORT);

    @Container
    final static GenericContainer<?> mongoMainContainer = new GenericContainer<>("mongo:latest")
            .withNetwork(mainNetwork)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "MainUser")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "Test123!")
            .withEnv("MONGO_INITDB_DATABASE", "CurrencyRates")
            .withExposedPorts(LOGS_DB_PORT);

    @BeforeAll
    public static void init() {
        mongoLogContainer.setPortBindings(List.of("27037:27017"));
        mongoLogContainer.start();

        mongoMainContainer.setPortBindings(List.of("27017:27017"));
        mongoMainContainer.start();

        appContainer = new GenericContainer<>("currency-rate-extractor:latest")
                .withNetwork(mainNetwork)
                .withExposedPorts(APP_PORT);
        var env = List.of(
                String.format("MongoCluster:BaseUrl=mongodb://host.docker.internal:%d/", mongoMainContainer.getFirstMappedPort()),
                String.format("MongoLog:BaseUrl=mongodb://host.docker.internal:%d/", mongoLogContainer.getFirstMappedPort()),
                String.format("ASPNETCORE_URLS=http://*:%d", APP_PORT),
                "ASPNETCORE_ENVIRONMENT=Test",
                "MongoCluster:UserName=MainUser",
                "MongoCluster:Password=Test123!",
                "MongoLog:UserName=MainUser",
                "MongoLog:Password=Test123!"
        );
        appContainer.setEnv(env); // Override all .withEnv() methods.
        appContainer.setPortBindings(List.of("8080:8080"));
        appContainer.start();

        appHost = appContainer.getHost();
        mainDBHost = mongoMainContainer.getHost();
        logsDBHost = mongoLogContainer.getHost();
        appPort1 = appContainer.getFirstMappedPort();
    }

    @AfterAll
    public static void stopItAll() {
        appContainer.stop();
        mongoMainContainer.stop();
        mongoLogContainer.stop();
    }
}