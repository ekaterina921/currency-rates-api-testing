package api;

import Utilities.Utils;

import com.mongodb.reactivestreams.client.MongoCollection;
import org.junit.jupiter.params.provider.CsvSource;
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
        mainDB = mongoClientMainDB.getDatabase("CurrencyRates");
        MongoCollection<Document> collectionLogs = mainDB.getCollection(currencyCode.toLowerCase());
        Publisher<Long> ratesDoc = collectionLogs.countDocuments();
        var beforeRatesNum = Mono.from(ratesDoc).block();
        beforeRatesNum = (beforeRatesNum== null) ? 0L : beforeRatesNum;
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
        //Validate Database records
        collectionLogs = mainDB.getCollection(currencyCode.toLowerCase());
        ratesDoc = collectionLogs.countDocuments();
        var ratesNum = Mono.from(ratesDoc).block();
        Assertions.assertNotNull(ratesNum, "The number of currency rates is null. It should be > 0.");
        Assertions.assertTrue(ratesNum > beforeRatesNum, "There should be more records in Currency Rates database.");
    }

    @DisplayName("Verify that duplicates are not saved in the Currency Rates database.")
    @ParameterizedTest
    @CsvSource({ "EUR, 2025-02-01"})
    public void testGetCurrencyEndpointRequestingTheSameDatesSeveralTimes(String currencyCode, String pastDate) {
        mainDB = mongoClientMainDB.getDatabase("CurrencyRates");
        //request <Past Date> for the first time
        given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        MongoCollection<Document> collectionLogs = mainDB.getCollection(currencyCode.toLowerCase());
        Publisher<Long> ratesDoc = collectionLogs.countDocuments();
        var beforeRatesNum = Mono.from(ratesDoc).block();
        Assertions.assertNotNull(beforeRatesNum, "The number of currency rates is null. It should be > 0.");
        //Send request for <Past Date> for the second time
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("api/Json_Schema_Currency_Rates.json"));
        //Validate Database records
        ratesDoc = collectionLogs.countDocuments();
        var ratesNum = Mono.from(ratesDoc).block();
        Assertions.assertNotNull(ratesNum, "The number of currency rates is null. It should be > 0.");
        Assertions.assertEquals(ratesNum, beforeRatesNum, "The number of records shouldn't have changed in Currency Rates database.");
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

    @DisplayName("Verify that proper error message is displayed for unsupported currencies.")
    @ParameterizedTest
    @CsvSource({ "RSD, 2025-02-01"})
    public void testGetCurrencyEndpointUnsupportedCurrencies(String currencyCode, String pastDate) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo(String.format("%s is not supported", currencyCode)));
    }

    @DisplayName("Verify that application does not fail if non-parsable date format is used.")
    @ParameterizedTest
    @CsvSource({ "EUR, Mar"})
    public void testGetCurrencyEndpointWrongDateFormat(String currencyCode, String wrongDate) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, wrongDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(400);
    }

    @DisplayName("Verify that application logs are recorded in the database.")
    @Test
    public void testLogging(){
        //Count the number of log documents in the database
        String collectionName = "log_" + (new Utils()).getTodaysDate().replaceAll("-", "");
        MongoCollection<Document> collectionMain = logDB.getCollection(collectionName);
        Publisher<Long> countMain = collectionMain.countDocuments();
        var mainRecordsNum = Mono.from(countMain)
                .block();
        //Check that log documents exist
        Assertions.assertNotNull(mainRecordsNum, "The number of records is null. It should be > 0.");
        Assertions.assertTrue(mainRecordsNum.intValue() > 0, "There should be more than 0 records in Logs.");
    }
}