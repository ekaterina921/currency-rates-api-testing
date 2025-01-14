# currency-rates-api-testing

# Currency rates extractor application

The application is aimed at speeding up the processes of conversion into other currencies. The application extracts currency rates from the following Banks: Bank of Canada, European Central Bank (ECB) and Reserve Bank of Australia (RBA).
The application is able to extract currency reates for EUR, USD and other currencies available via the above-mentioned banks' API.
The user should be able to preform the actions below:
- view the supported currencies, 
- review if the target currency is supported
- view the rates in relation to a certain supported currency for a specific day in the past.

## Technical details:
There are 3 endpoints:
- /supported-currency - returns list of supported currencies
- /supported-currency/{currency-code} - returns boolean value stating if the specified currency is supported
- /rates/{currency-code}/{YYYY-MM-DD} - returns currency rates for the specified currency for the specified date in the past

2 MongoDB instances are used to support application:
- MongoDB to store fetched currency rates (they are used to speed up the response in case of requesting the /rates/{currency-code}/{YYYY-MM-DD} endpoint more than once)
- MongoDB to store logs (can be used for troubleshooting or collecting statistics)