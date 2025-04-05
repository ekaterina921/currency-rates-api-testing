## Bug 1 Priority High "Currency rates are not returned for USD and GBP"
### Steps: 
1. Run currency rates endpoint http://{appHost}:{appPort}/rates/{currency-code}/{YYYY-MM-DD}
**where** appHost and appport are current app host and port 
currency-code is USD or GBP.
YYYY-MM-DD is a date in the near past, e.g., 2025-04-01
### Expected result: 
200 "OK" response is received. Currency rates for requested currency-code and date are returned in the response body.
[Screenshot](https://drive.google.com/file/d/1-xb9XGkgxSy7C7Kl8meyn-yWWslWv4Qi/view?usp=drive_link)
### Actual result: 
500 "Internal Server Error" response is received.
[Screenshot 1](https://drive.google.com/file/d/1-v_1lOHX9RJV-LibQRBZxKLq6CI5amBN/view?usp=sharing)
[Screenshot 2](https://drive.google.com/file/d/1-u7v_d0-2QDjCQcXQNwGaZRoW70YoFts/view?usp=drive_link)

## Improvement 2 Priority Medium "Clear error message should be returned in case currency rates for non-parsable date are requested"
### Steps:
1. Run currency rates endpoint http://{appHost}:{appPort}/rates/{currency-code}/{YYYY-MM-DD}
**where** appHost and appport are current app host and port 
currency-code is USD or GBP.
**YYYY-MM-DD** is a non-parsable date, e.g., Mar
### Expected result: 
Like in the case with unsupported currency codes, 400 code with a clear error message in the body is returned. E.g., {"error" : "Mar is incorrect date. Please update it using YYYY-MM-DD format." }
[Screenshot](https://drive.google.com/file/d/1-yHl8AVXw2eyWd5ZdyvYWQetEp6RhZSv/view?usp=drive_link)
### Actual result:
400 code with a technical error message is returned. It is not clear what exactly is wrong with the request.
[Screenshot](https://drive.google.com/file/d/102pr38A815f35JJW2UQ5QEOmDZH4lS5n/view?usp=drive_link)