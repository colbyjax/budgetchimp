package io.fouri;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import io.fouri.domain.ChaseTransaction;
import io.fouri.domain.Transaction;

import java.io.*;
import java.util.List;

public class TransactionHandler {
    public static void handleTransaction(String srcBucket, String srcKey)  {
        List<ChaseTransaction> transactions = null;

        // Retrieve File contents
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(
                srcBucket, srcKey));
        InputStream fileStream = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
        transactions = new CsvToBeanBuilder(reader)
                .withType(ChaseTransaction.class).build().parse();

        try {
            if (transactions != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                for (ChaseTransaction transaction : transactions) {
                    System.out.println(objectMapper.writeValueAsString(transaction));
                }
            } else {
                System.out.println("Transactions empty!");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
