package io.fouri;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<S3Event, String> {
    private final S3Client s3Client;
    private static final Logger logger = LogManager.getLogger(App.class);

    public App() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();

        String srcKey = record.getS3().getObject().getUrlDecodedKey();
        logger.info("Received Lambda Trigger from S3: " + srcKey);
        TransactionHandler.handleTransaction(srcBucket,srcKey);

        return "Ok";
    }
}
