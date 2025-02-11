package api;

import static api.BaseTestStartEnd.appHost;
import static api.BaseTestStartEnd.appPort;

public interface EndpointsList {
    String BASEURI = "http://" + appHost + ":" + appPort;
    String SUPPORTED_CURRENCIES_ENDPOINT = "/supported-currency";
    String SUPPORTED_CURRENCY_ENDPOINT = "/supported-currency/{currency-code}";
    String CURRENCY_RATES_ENDPOINT = "/rates/{currency-code}/{YYYY-MM-DD}";
}
