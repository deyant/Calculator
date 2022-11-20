# Getting Started

### Requirements

* JDK 17

### Build and run tests

> ./mvnw package

### Start the application

> ./mvnw spring-boot:run

or 

> java -jar target/Calculator-0.0.1-SNAPSHOT.jar

### Testing

1. Open Swagger UI with a web browser: 
> http://localhost:8080

2. Expand the API:
> /api/v1/sumInvoices

3. Click the button ***"Try it out"***

4. Fill the parameters:
<ul>
  <li>exchangeRates</li>
  <li>outputCurrency</li>
  <li>file (select a CSV file)</li>
  <li>customerVat (optional)</li>
</ul>

5. Click the button ***Execute***

