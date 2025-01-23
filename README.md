# Currency rates extractor application

## Requirements
The application is aimed at speeding up the processes of conversion into other currencies. The application extracts currency rates from the following Banks: Bank of Canada, European Central Bank (ECB) and Reserve Bank of Australia (RBA).
The application is able to extract currency rates for EUR, USD, GBP, AUD, CAD available via the mentioned banks' API.
The user should be able to preform the actions below:
- view the supported currencies, 
- review if the target currency is supported
- view the rates in relation to a certain supported currency for a specific day in the past.

## Technical details
There are 3 endpoints:
- /supported-currency - returns list of supported currencies
- /supported-currency/{currency-code} - returns boolean value stating if the specified currency is supported
- /rates/{currency-code}/{YYYY-MM-DD} - returns currency rates for the specified currency for the specified date in the past

2 MongoDB instances are used to support application:
- MongoDB to store fetched currency rates (they are used to speed up the response in case of requesting the /rates/{currency-code}/{YYYY-MM-DD} endpoint more than once),
- MongoDB to store logs (can be used for troubleshooting or collecting statistics)

## How to run tests on local machine
Pre-conditions:
Java (OpenJDK 23.0.1), Docker and IntelliJ IDEA are installed on the local machine.
currency-rate-tests repository is downloaded.

I. Create docker image
1. Start Docker
1. Open Terminal
1. In terminal, navigate to currency-rates-api-testing -> app folder
1. Execute:
`docker build -t currency-rate-extractor -f Web/Dockerfile .`

II. Run tests
1. Make sure that Docker is started.
1. Open currency-rates-api-testing -> tests folder in IntelliJ
1. Click Start button next to the ApiTests class (The environment will be started and stopped automatically. Latest Docker image of the application and an image of the latest version of MongoDB will be used to create the environment).