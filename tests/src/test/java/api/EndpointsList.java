package api;

import static api.BaseTestStartEnd.appHost;
import static api.BaseTestStartEnd.appPort1;

public interface EndpointsList {
    String SUPPORTED_CURRENCIES_ENDPOINT = "http://" + appHost + ":" + appPort1 + "/supported-currency";
    String SUPPORTED_CURRENCY_ENDPOINT = "http://" + appHost + ":" + appPort1 +"/supported-currency/{currency-code}";
    String CURRENCY_RATES_ENDPOINT = "http://" + appHost + ":" + appPort1 + "/rates/{currency-code}/{YYYY-MM-DD}";
}
