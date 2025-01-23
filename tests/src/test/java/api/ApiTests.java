package api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;

@Testcontainers(parallel = true)
public class ApiTests extends BaseTestStartEnd implements EndpointsList {

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

    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrenciesWithPastDates.csv")
    public void TestGetCurrencyEndpointPositive(String currencyCode, String pastDate) {
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        response.then().assertThat().statusCode(200);
    }

    @Test
    public void TestGetCurrencyEndpointYesterdaysDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate yesterdayDate = currentDate.minusDays(1);

        given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, "EUR", yesterdayDate.toString())
                .then()
                .assertThat().statusCode(200);
    }
}