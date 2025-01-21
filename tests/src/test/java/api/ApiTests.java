package api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
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
        given()
                .when()
                .get(SUPPORTED_CURRENCIES_ENDPOINT)
                .then()
                .assertThat()
                .statusCode(200);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrencies.csv")
    public void TestSupportedCurrencyEndpointPositive(String currencyCode) {
        given()
                .when()
                .get(SUPPORTED_CURRENCY_ENDPOINT, currencyCode)
                .then()
                .assertThat()
                .statusCode(200);
    }


    @Test
    public void TestGetCurrencyEndpointPositive() {
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, "EUR", "2024-12-12");
        response.then().assertThat().statusCode(200);
    }
}
