package api;

import static api.BaseTestStartEnd.appHost;
import static api.BaseTestStartEnd.appPort;

public interface EndpointsList {
    String SUPPORTED_CURRENCIES_ENDPOINT = "http://" + appHost + ":" + appPort + "/supported-currency";
    String SUPPORTED_CURRENCY_ENDPOINT = "http://" + appHost + ":" + appPort + "/supported-currency/{currency-code}";
    String CURRENCY_RATES_ENDPOINT = "http://" + appHost + ":" + appPort + "/rates/{currency-code}/{YYYY-MM-DD}";
}
