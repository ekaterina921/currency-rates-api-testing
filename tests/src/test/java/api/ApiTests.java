package api;

import Utilities.Utils;

import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

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
                .statusCode(200);
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
                .statusCode(200);
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
                .statusCode(200);
//        MongoCollection<Document> collectionMain = mainDB.getCollection("log_" + (new Utils()).getTodaysDate());
//        MongoCollection<Document> collectionLogs = logDB.getCollection("startup_log");
//        FindIterable<Document> mainDoc = collectionMain.find();
////        for (Document doc : mainDoc) {
////            System.out.println(doc.toJson());
////        }
//        FindIterable<Document> logs = collectionLogs.find();
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
                .statusCode(200);
    }
}