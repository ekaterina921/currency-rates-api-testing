package api;

import Utilities.Utils;

import com.mongodb.reactivestreams.client.MongoCollection;
import org.reactivestreams.Publisher;
import org.bson.Document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testcontainers.junit.jupiter.Testcontainers;


import reactor.core.publisher.Mono;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Testcontainers(parallel = true)
public class ApiTests extends BaseTestStartEnd implements EndpointsList {

    @DisplayName("Verify the list of supported currencies is displayed by Supported Currencies endpoint.")
    @Test
    public void testSupportedCurrenciesEndpoint() {
        //Send request
        var response = given()
                .when()
                .get(SUPPORTED_CURRENCIES_ENDPOINT);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body("", containsInAnyOrder("EUR", "USD", "GBP", "AUD", "CAD"));
        // containsInAnyOrder tests that only supported currencies are returned in any order
    }

    @DisplayName("Verify that true is returned by Supported Currency endpoint in case the currency is supported")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrencies.csv")
    public void testSupportedCurrencyEndpointPositive(String currencyCode) {
        //Send request
        var response = given()
                .when()
                .get(SUPPORTED_CURRENCY_ENDPOINT, currencyCode);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body("isSupported", equalTo(true));
    }

    @DisplayName("Verify that false is returned by Supported Currency endpoint in case the currency is not supported")
    @ParameterizedTest
    @CsvFileSource(resources = "NotSupportedCurrencies.csv")
    public void testSupportedCurrencyEndpointNegative(String currencyCode) {
        //Send request
        var response = given()
                .when()
                .get(SUPPORTED_CURRENCY_ENDPOINT, currencyCode);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body("isSupported", equalTo(false));
    }

    @DisplayName("Verify that Supported Currencies With Past Dates endpoint returns the list of currency rates")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrenciesWithPastDates.csv")
    public void testGetCurrencyEndpointPastDates(String currencyCode, String pastDate) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("api/Json_Schema_Currency_Rates.json"));
        mainDB = mongoClientMainDB.getDatabase("CurrencyRates");
        MongoCollection<Document> collectionLogs = mainDB.getCollection(currencyCode.toLowerCase());
 //       String id = String.format("%s 00:00:00.000+00:00", pastDate);
        Publisher<Long> ratesDoc = collectionLogs.countDocuments();
        var ratesNum = Mono.from(ratesDoc).block();
        Assertions.assertNotNull(ratesNum, "The number of currency rates is null. It should be > 0.");
        Assertions.assertTrue(ratesNum.intValue() > 0, "There should be more than 0 records in Currency Rates.");
    }

    @DisplayName("Verify valid date boundary value is correctly processed by Supported Currencies endpoint.")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrencies.csv")
    public void testGetCurrencyEndpointYesterdaysDate(String currencyCode) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, (new Utils()).getYesterdaysDate());
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("api/Json_Schema_Currency_Rates.json"));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrencies.csv")
    public void testGetCurrencyEndpointTodaysDate(String currencyCode) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, (new Utils()).getTodaysDate());
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("Date is not in the past."));
    }


    @DisplayName("Verify that Supported Currencies With Future Dates endpoint returns error.")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrenciesWithFutureDates.csv")
    public void testGetCurrencyEndpointFutureDates(String currencyCode, String pastDate) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("Date is not in the past."));
    }

    @DisplayName("Verify that application logs are recorded in the database.")
    @Test
    public void testLogging(){
        //Count the number of log documents in the database
        String collectionName = "log_" + (new Utils()).getTodaysDate();
        MongoCollection<Document> collectionMain = logDB.getCollection(collectionName);
        Publisher<Long> countMain = collectionMain.countDocuments();
        var mainRecordsNum = Mono.from(countMain)
                .block();
        //Check that log documents exist
        Assertions.assertNotNull(mainRecordsNum, "The number of records is null. It should be > 0.");
        Assertions.assertTrue(mainRecordsNum.intValue() > 0, "There should be more than 0 records in Logs.");
    }
}