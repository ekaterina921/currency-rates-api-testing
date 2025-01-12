package api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
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

    //    @Container
    static GenericContainer<?> appContainer = null;

    @Container
    final static MongoDBContainer databaseStorageContainer = new MongoDBContainer("mongo:latest")
            .withNetwork(mainNetwork)
            .withExposedPorts(MAIN_DB_PORT);
    @Container
    final static MongoDBContainer databaseLogsContainer = new MongoDBContainer("mongo:latest")
            .withNetwork(mainNetwork)
            .withExposedPorts(LOGS_DB_PORT);

    @BeforeAll
    public static void init() {
        databaseStorageContainer.start();
        databaseLogsContainer.start();

        appContainer = new GenericContainer<>("currency-rate-extractor:latest")
                .withNetwork(mainNetwork)
                .withExposedPorts(APP_PORT);
        var env = List.of(
                String.format("MongoCluster:BaseUrl=mongodb://host.docker.internal:%d/", databaseStorageContainer.getFirstMappedPort()),
                String.format("MongoLog:BaseUrl=mongodb://host.docker.internal:%d/", databaseLogsContainer.getFirstMappedPort()),
                String.format("ASPNETCORE_URLS=http://*:%d", APP_PORT),
                "ASPNETCORE_ENVIRONMENT=Test",
                "MongoCluster__UserName=MainUser",
                "MongoCluster__Password=Test123!",
                "MongoLog__UserName=MainUser",
                "MongoLog__Password=Test123!"
        );
        appContainer.setEnv(env); // Override all .withEnv() methods.
        appContainer.setPortBindings(List.of("8080:8080"));
        appContainer.start();

        appHost = appContainer.getHost();
        mainDBHost = databaseStorageContainer.getHost();
        logsDBHost = databaseLogsContainer.getHost();
        appPort1 = appContainer.getFirstMappedPort();
    }

    @AfterAll
    public static void stopItAll() {
        appContainer.stop();
        databaseStorageContainer.stop();
        databaseLogsContainer.stop();
    }

}
