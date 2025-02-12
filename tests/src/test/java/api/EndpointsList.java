package api;

import static api.BaseTestStartEnd.appHost;
import static api.BaseTestStartEnd.appPort;

public interface EndpointsList {
    String BASEURI = "http://" + appHost + ":" + appPort;
    String SUPPORTED_CURRENCIES_ENDPOINT = BASEURI + "/supported-currency";
    String SUPPORTED_CURRENCY_ENDPOINT = BASEURI + "/supported-currency/{currency-code}";
    String CURRENCY_RATES_ENDPOINT = BASEURI + "/rates/{currency-code}/{YYYY-MM-DD}";
}
