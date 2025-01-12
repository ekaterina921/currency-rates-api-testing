package api;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(parallel = true)
public class ApiTests extends BaseTestStartEnd implements EndpointsList {

    @Test
    public void TestContainersSetup() {
        assertAll(
                () -> assertTrue(appContainer.isRunning(), "App container should be running"),
                () -> assertTrue(databaseStorageContainer.isRunning(), "DB storage container should be running"),
                () -> assertTrue(databaseLogsContainer.isRunning(), "Logs storage container should be running")
        );
    }

    @Test
    public void TestSupportedCurrenciesEndpoint() {
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/supported-currency";
        given()
                .when()
                .get(pathToGet)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void TestSupportedCurrencyEndpointPositive() {
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/supported-currency/EUR";
        given()
                .when()
                .get(pathToGet)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void TestSupportedCurrencyEndpointNegative(){
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/supported-currency/RSD";
        given()
                .when()
                .get(pathToGet)
                .then()
                .assertThat()
                .statusCode(200);
    }


    @Test
    public void TestGetCurrencyEndpointPositive() {
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/rates/EUR/2024-12-12";
        var response = given()
                .when()
                .get(pathToGet);
        response.then().assertThat().statusCode(200);
    }

    @Test
    public void TestGetCurrencyEndpointUnsupportedCurrency(){
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/rates/RSD/2024-12-12";
        var response = given()
                .when()
                .get(pathToGet);
        response.then().assertThat().statusCode(200);
    }
    @Test
    public void TestGetCurrencyEndpointNotPastDate(){
        String pathToGet = "http://" + appHost + ":" + appPort1 + "/rates/EUR/2028-12-12";
        var response = given()
                .when()
                .get(pathToGet);
        response.then().assertThat().statusCode(200);
    }
}
