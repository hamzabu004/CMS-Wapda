# CMS-Wapda Testing Guide

This project contains unit tests implemented using JUnit 5 and Mockito. Test cases are located under `src/test/java/testcases/`.

## Running the Unit Tests

You can run the tests via Maven. To run all tests and view results in the terminal, run the following command in the project root directory:

```bash
./mvnw clean test
```
Or if you have a local maven installation:
```bash
mvn clean test
```

## Adding More Tests
Place any new test files inside `src/test/java/testcases/` with the `Test.java` suffix so Maven Surefire picks them up automatically.
