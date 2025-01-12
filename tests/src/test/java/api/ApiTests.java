package api;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(parallel = true)
public class ApiTests extends BaseTestStartEnd {

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


    @Test
    public void GetCurrencyEndpointTest() {
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/rates/EUR/2024-12-12";
        var response = given()
                .when()
                .get(pathToGet);
        response.then().assertThat().statusCode(200);
    }
}
