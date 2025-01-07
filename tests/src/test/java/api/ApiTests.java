package api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(parallel = true)
public class ApiTests {
    private final static Network mainNetwork = Network.newNetwork();
    private final static int APP_PORT = 8080;
    private final static int MAIN_DB_PORT = 27017;
    private final static int LOGS_DB_PORT = 27037;
    static String appHost;
    static String mainDBHost;
    static String logsDBHost;
    static int appPort1;

    @Container
    private final static GenericContainer<?> appContainer = new GenericContainer<>("currency-rate-extractor:latest")
            .withEnv("ASPNETCORE_URLS", "http://*:8080")
            .withEnv("MongoCluster:BaseUrl", "mongodb://host.docker.internal:27017/")
            .withEnv("MongoCluster:UserName", "MainUser")
            .withEnv("MongoCluster:Password", "Test123!")
            .withEnv("MongoLog:BaseUrl", "mongodb://host.docker.internal:27037/")
            .withEnv("MongoLog:UserName", "MainUser")
            .withEnv("MongoLog:Password", "Test123!")
            .withNetwork(mainNetwork)
            .withExposedPorts(APP_PORT);
    @Container
    private final static MongoDBContainer databaseStorageContainer = new MongoDBContainer("mongo:latest")
            .withNetwork(mainNetwork)
            .withExposedPorts(MAIN_DB_PORT);
    @Container
    private final static MongoDBContainer databaseLogsContainer = new MongoDBContainer("mongo:latest")
            .withNetwork(mainNetwork)
            .withExposedPorts(LOGS_DB_PORT);

    @BeforeAll
    public static void init() {
        appContainer.start();
        databaseStorageContainer.start();
        databaseLogsContainer.start();
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


    @Test
    public void containersSetupTest() {
        assertAll(
                () -> assertTrue(appContainer.isRunning(), "App container should be running"),
                () -> assertTrue(databaseStorageContainer.isRunning(), "DB storage container should be running"),
                () -> assertTrue(databaseLogsContainer.isRunning(), "Logs storage container should be running")
        );
    }

    @Test
    public void supportedCurrencyEndpointTest() {
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/supported-currency";
        given()
                .when()
                .get(pathToGet)
                .then()
                .assertThat()
                .statusCode(200);
    }

}
