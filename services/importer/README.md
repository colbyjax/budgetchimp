# App

This project contains an AWS Lambda maven application with [AWS Java SDK 2.x](https://github.com/aws/aws-sdk-java-v2) dependencies.

## Prerequisites
- Java 1.8+
- Apache Maven
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
- Docker

## Development

*  App - Main entry point of application. Responsible for listing to S3 events and determining transaction file type and sending 
to the correct Handler.
* Handlers - TransactionHandler is the main abstract class that other Handlers are derived. It houses most
of the logic needed to handle the S3 files and insertions into the Database.  Handler classes derived from this should
only contain logic specific to converting institution specific transactions into generic transactions for TransactionHandler
to manage.
* Shared Package contains all classes and configuations that will be shared for BudgetChimp. This includes
domain objects, DAO accessors, and configuration files.

#### Building the project
```
mvn clean install
```

#### Generating S3 event to test locally
```
sam local generate-event s3 put --bucket budgetchimp/transactions-intake --key cc-dump-1.csv > s3_event.json
```
#### Testing it locally
```
sam local invoke
sam local invoke AppFunction --event s3_event.json
```



#### Adding more SDK clients
To add more service clients, you need to add the specific services modules in `pom.xml` and create the clients in `DependencyFactory` following the same 
pattern as s3Client.

## Deployment

The generated project contains a default [SAM template](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-resource-function.html) file `template.yaml` where you can 
configure different properties of your lambda function such as memory size and timeout. You might also need to add specific policies to the lambda function
so that it can access other AWS resources.

To deploy the application, you can run the following command:

```
sam deploy --guided
```

See [Deploying Serverless Applications](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-deploying.html) for more info.



