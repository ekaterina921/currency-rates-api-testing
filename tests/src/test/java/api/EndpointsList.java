package api;

public interface EndpointsList {
    String SUPPORTED_CURRENCIES_ENDPOINT = "http://{appHost}:{appPort1}/supported-currency";
    String SUPPORTED_CURRENCY_ENDPOINT = "http://{appHost}:{appPort1}/supported-currency/{currency-code}";
    String CURRENCY_RATES_ENDPOINT = "http://{appHost}:{appPort1}/rates/{currency-code}/{YYYY-MM-DD}";
}
