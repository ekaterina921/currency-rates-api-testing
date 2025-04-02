package api;

import Utilities.Utils;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.reactivestreams.Publisher;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@Testcontainers(parallel = true)
public class ApiTests extends BaseTestStartEnd implements EndpointsList {

    @DisplayName("Verify the list of supported currencies is displayed by Supported Currencies endpoint.")
    @Test
    public void TestSupportedCurrenciesEndpoint() {
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
    public void TestSupportedCurrencyEndpointPositive(String currencyCode) {
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
    public void TestSupportedCurrencyEndpointNegative(String currencyCode) {
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

    @DisplayName("Verify that Supported Currencies With Past Dates endpoint returns the list of currency rates and saves to DB")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrenciesWithPastDates.csv")
    public void TestGetCurrencyEndpointPositive(String currencyCode, String pastDate) {
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
        //        mainDB = mongoClientMainDB.getDatabase("CurrencyRates");
        //        MongoCollection<Document> collectionLogs = mainDB.getCollection(currencyCode);
//        String id = String.format("%sT00:00:00.000+00:00", pastDate);
        //        Publisher<Long> ratesDoc = collectionLogs.countDocuments();
    }

    @DisplayName("Verify valid date boundary value is correctly processed by Supported Currencies endpoint.")
    @ParameterizedTest
    @CsvFileSource(resources = "SupportedCurrencies.csv")
    public void TestGetCurrencyEndpointYesterdaysDate(String currencyCode) {
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
    public void TestGetCurrencyEndpointTodaysDate(String currencyCode) {
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
    public void TestGetCurrencyEndpointNegative(String currencyCode, String pastDate) {
        //Send request
        var response = given()
                .when()
                .get(CURRENCY_RATES_ENDPOINT, currencyCode, pastDate);
        //Validate response
        response
                .then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("Date is not in the past."));;
    }

    @DisplayName("Verify that application logs are recorded in the database.")
    @Test
    public void TestLogging(){
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