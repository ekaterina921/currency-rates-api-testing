package api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

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
    final static GenericContainer<?> mongoLogContainer = new GenericContainer<>("mongo:latest")
            .withNetwork(mainNetwork)
            .withEnv("MONGO_INITDB_ROOT_USERNAME","MainUser")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD","Test123!")
            .withEnv("MONGO_INITDB_DATABASE","Logging")
            .withExposedPorts(27017);

    @Container
    final static GenericContainer<?> mongoMainContainer = new GenericContainer<>("mongo:latest")
            .withNetwork(mainNetwork)
            .withEnv(Map.of("MONGO_INITDB_ROOT_USERNAME", "MainUser",
                    "MONGO_INITDB_ROOT_PASSWORD", "Test123!",
                    "MONGO_INITDB_DATABASE", "CurrencyRates"))
            .withExposedPorts(27017);

//    @Container
//    final static MongoDBContainer databaseStorageContainer = new MongoDBContainer("mongo:latest")
//            .withNetwork(mainNetwork)
//            .withExposedPorts(MAIN_DB_PORT)
//            .withEnv("UserName", "MainUser")
//            .withEnv("Password", "Test123!");

//    @Container
//    final static MongoDBContainer databaseLogsContainer = new MongoDBContainer("mongo:latest")
//            .withNetwork(mainNetwork)
//            .withExposedPorts(LOGS_DB_PORT)
//            .withEnv("UserName", "MainUser")
//            .withEnv("Password", "Test123!");

    @BeforeAll
    public static void init() {
        mongoLogContainer.setPortBindings(List.of("27037:27017"));
        mongoLogContainer.start();

        mongoMainContainer.setPortBindings(List.of("27017:27017"));
        mongoMainContainer.start();

//        databaseStorageContainer.start();
//        databaseLogsContainer.start();

        appContainer = new GenericContainer<>("currency-rate-extractor:latest")
                .withNetwork(mainNetwork)
                .withExposedPorts(APP_PORT);
//        databaseLogsContainer.setPortBindings(List.of("27037:27017"));
//        databaseStorageContainer.setPortBindings(List.of("27017:27017"));
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

        var port = mongoLogContainer.getExposedPorts();
        var port1 = mongoMainContainer.getExposedPorts();
    }

    @AfterAll
    public static void stopItAll() {
        appContainer.stop();
        mongoMainContainer.stop();
        mongoLogContainer.stop();

//        databaseStorageContainer.stop();
//        databaseLogsContainer.stop();
    }

}
