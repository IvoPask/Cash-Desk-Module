# Cash Desk Module

* The Cash Desk Module is a Spring Boot-based backend application that manages deposit and withdrawal operations for multiple cashiers in BGN and EUR currencies and stores the transaction and balance history securely in TXT files.
 
## Tech

- Java 17
- SpringBoot 3
- Spring Security (API key validation)
- Maven
- Project Lombok
- JUnit 5 (Testing)
- Postman (API collection and environment provided)

## Main features

* Initialize cashiers and store them in an in-memory data structure during the first application startup, while also creating the `balance_history.txt` file.  
	On subsequent application runs, the system reads the latest balances from the file and repopulates the in-memory structure to ensure data continuity.
* Support deposits and whitdrawals in BGN and EUR
* Check cash balances with optional filters: 
	* by cashier name 
	* by range date(`dateFrom`/`dateTo`)
* Store transaction history and balance history into structured TXT files
* Full request validation and logging using Slf4j

## Environment variables

- **REQUEST_HEADER** — Custom request header key for authentication.

- **API_KEY** — API key required for all authenticated requests.

- **TRANSACTION_HISTORY_FILE** — File path where transaction history will be stored.  
  Default: `./var/log/cashdesk/transaction_history.txt`

- **BALANCE_HISTORY_FILE** — File path where balance history will be stored.  
  Default: `./var/log/cashdesk/balance_history.txt`


## Building the application
* Execute "mvnw clean install -DskipTests" in the root folder of the project to build without tests, or "mvnw clean install" to build with tests

## Running the application
* Execute "mvnw spring-boot:run" in the root folder of the project

## Testing via Postman
* Import the provided Postman collection (`postman`/ folder)
* Import the Postman environment (`postman`/ folder)
* Execute collection