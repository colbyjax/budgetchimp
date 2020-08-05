package io.fouri;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import io.fouri.domain.ChaseTransaction;
import io.fouri.domain.Transaction;
import io.fouri.shared.ConfigProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.util.List;

public class TransactionHandler {
    private static final ConfigProvider config = new ConfigProvider();
    private static final Logger logger = LogManager.getLogger(TransactionHandler.class);

    public static void handleTransaction(String srcBucket, String srcKey)  {
        List<Transaction> transactions = null;
        boolean successfulImport = true;

        // Retrieve File contents
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
        InputStream fileStream = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));

        // Convert CSV to structured POJO
        try {
            transactions = new CsvToBeanBuilder(reader)
                    .withType(ChaseTransaction.class).build().parse();
            // Convert transactions to JSON
            try {
                if (transactions != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    for (ChaseTransaction transaction : transactions) {
                        logger.info(objectMapper.writeValueAsString(transaction));
                        break;
                    }
                    successfulImport = true;
                } else {
                    logger.error("Transactions empty!");
                    successfulImport = false;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                logger.error("Exception thrown: Invalid JSON: " + srcKey);
                successfulImport = false;
            }

        } catch (IllegalStateException e) {
            logger.error("Exception thrown: Invalid CSV File: " + srcKey);
            e.printStackTrace();
            successfulImport = false;
        }

        // If running debug mode, do not move the file
        if(!config.get("run-mode").equals("debug")) {
            boolean moveFileStatus = moveFile(successfulImport, srcBucket, srcKey, s3Client);
            if (moveFileStatus) {
                logger.info("Successfully Moved File: " + srcKey);
            } else {
                logger.error("Error moving file: " + srcKey);
            }
        }
    }

    /**
     * Helper function to import transactions into the database.
     * @param transactions - Transactions from the CSV file
     * @return true if successfully loaded into database, otherwise false
     */
    public static boolean importTransactions(List<Transaction> transactions) {
        //TODO: Implement Database Logic
        return true;
    }


    /**
     * Helper function to move the file after processing is completed to either 'processed' or 'error'
     * @return
     */
    private static boolean moveFile(boolean successfulImport, String srcBucket, String srcKey, AmazonS3 s3Client) {
        if(successfulImport) {
            String successBucket = config.get("success-bucket");

            s3Client.copyObject(srcBucket, srcKey, successBucket, srcKey);
            s3Client.deleteObject(new DeleteObjectRequest(srcBucket, srcKey));
            logger.info("Moving SUCCESS Import File: " + srcKey);
            return true;
        } else {
            String errorBucket = config.get("error-bucket");
            s3Client.copyObject(srcBucket, srcKey, errorBucket, srcKey);
            s3Client.deleteObject(new DeleteObjectRequest(srcBucket, srcKey));
            logger.info("Moving ERROR Import File: " + srcKey);
            return false;
        }
    }

}
