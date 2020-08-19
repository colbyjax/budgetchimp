package io.fouri.budgetchimp.handlers;


import io.fouri.budgetchimp.DependencyFactory;
import io.fouri.budgetchimp.dao.TransactionDao;
import io.fouri.budgetchimp.domain.Transaction;
import io.fouri.budgetchimp.shared.ConfigProvider;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
public abstract class TransactionHandler {
    private final ConfigProvider config = new ConfigProvider();
    private final Logger logger = LogManager.getLogger(TransactionHandler.class);
    protected final String RUN_MODE = config.get("run-mode");
    private final String DYNAMO_TABLE = config.get("dynamo-table");
    private final String USER_PREFIX = config.get("user-prefix");
    private final String USER_SUFFIX = config.get("user-suffix");

    private S3Client s3;
    private String srcBucket;
    private String srcKey;
    private String file;
    @Setter
    private List<Transaction> transactions;


    public TransactionHandler(String srcBucket, String srcKey, String file) {
        this.srcBucket = srcBucket;
        this.srcKey = srcKey;
        this.s3 = DependencyFactory.s3Client();
        this.transactions = null;
        this.file = file;
    }

    /**
     * handleTransaction is the actual implementation of handling a specific transaction file. The caller (App) simply expects the transaction
     * to be handled and all state handled within here.  App does not need to be aware of success or failure.
     * The buck stops here.  Super calls should be used frequently as re-usable code should be put within Abstract (this) class
     */
    public abstract void handleTransaction();

    /**
     * Returns a Buffered reader from the s3 object that was identified during instantiation
     * @return Buffered Reader for S3 File
     */
    protected BufferedReader getFileReader() {
        ResponseInputStream response = s3.getObject(GetObjectRequest
                .builder()
                .bucket(srcBucket)
                .key(srcKey)
                .build());
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));
        return reader;
    }


    /**
     * Helper function to import transactions into the database.
     * @param transactions - Transactions from the CSV file
     * @return true if successfully loaded into database, otherwise false
     */
    protected boolean importTransactions(List<Transaction> transactions) {

        DynamoDbEnhancedClient ddb = DependencyFactory.dynamoDbClient();
        DynamoDbTable<TransactionDao> table = ddb.table(DYNAMO_TABLE, TableSchema.fromBean(TransactionDao.class));

        logger.debug("Importing Transactions into Database: " + transactions.size());

        int count = 0;
        for(Transaction transaction:transactions) {
            TransactionDao dao = new TransactionDao();
            String pk = USER_PREFIX + ":" + USER_SUFFIX;
            String sk = transaction.getTransactionDate() + ":" + transaction.getId();
            dao.setPk(pk);
            dao.setSk(sk);
            dao.setSource(transaction.getSource());
            dao.setTransactionDate(transaction.getTransactionDate());
            dao.setDescription(transaction.getDescription());
            dao.setCategory(transaction.getCategory());
            dao.setType(transaction.getType());
            dao.setAmount(transaction.getAmount());

            try {
                //TODO: Consider a  batch request
                table.putItem(dao);
            } catch (DynamoDbException e) {
                logger.error("Exception thrown while importing items into Database");
                e.printStackTrace();
                return false;
            }

            logger.debug("Transaction Imported: " + sk + ":" + transaction.getDescription());
            count++;
        }
        logger.info("Transactions Import Complete: " + count);
        return true;
    }

    /**
     * Helper function to move the file after processing is completed to either 'processed' or 'error'
     * @return
     */
    protected boolean moveFile(boolean successfulImport) {
        String destinationKey = (successfulImport) ?  ( config.get("success-prefix") + "/" + file )
                : ( config.get("error-prefix") + "/" + file);

        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(srcBucket + "/" + srcKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.error("URL could not be encoded: " + e.getMessage());
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(srcBucket)
                .key(srcKey)
                .build();
        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .copySource(encodedUrl)
                .destinationBucket(srcBucket)
                .destinationKey(destinationKey)
                .build();
        try {
            s3.copyObject(copyObjectRequest);
            s3.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            logger.error("Exception thrown while moving S3 File: " + srcKey);
            e.printStackTrace();
        }

        logger.info("Moved Import File: " + srcKey + " to " + destinationKey);
        return true;
    }

}
