# file-catalog-api
A RESTful API written in Java that allows users to upload and download files.

## How to build
Run the following command in the root project directory:
```bash
mvn package
```

## How to run
Start the server by executing the file-catalog-api-1.0.jar JAR located in .\target:
```bash
java -jar target\file-catalog-api-1.0.jar
```
The server will listen for requests on port 8484.

## Database details
The database is created automatically by the application in an H2 in-memory instance.
