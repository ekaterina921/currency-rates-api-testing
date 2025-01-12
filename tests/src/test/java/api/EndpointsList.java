package api;

public interface EndpointsList {
    static final String SUPPORTED_CURRENCIES_ENDPOINT = "http://{appHost}:{appPort1}/supported-currency";
    static final String SUPPORTED_CURRENCY_ENDPOINT = "http://{appHost}:{appPort1}/supported-currency/{currency-code}";
    static final String CURRENCY_RATES_ENDPOINT = "http://{appHost}:{appPort1}/rates/{currency-code}/{YYYY-MM-DD}";
}
