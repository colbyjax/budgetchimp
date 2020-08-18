package io.fouri.budgetchimp.budgetchimp.handlers;


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
    private final String RUN_MODE = config.get("run-mode");
    private final String DYNAMO_TABLE = config.get("dynamo-table");
    private final String USER_PREFIX = config.get("user-prefix");

    private S3Client s3;
    private String srcBucket;
    private String srcKey;
    @Setter
    private List<Transaction> transactions;


    public TransactionHandler(String srcBucket, String srcKey) {
        this.srcBucket = srcBucket;
        this.srcKey = srcKey;
        this.s3 = DependencyFactory.s3Client();
        this.transactions = null;
    }

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
     * Only method called from Subclasses to import into database and move files
     * @param transactions
     * @return
     */
    protected boolean processTransactions(List<Transaction> transactions) {
        boolean result = importTransactions(transactions);

        // If running debug mode, do not move the file
        if(!RUN_MODE.equals("debug")) {
            result = moveFile(result);
        }
        return result;
    }

    /**
     * Helper function to import transactions into the database.
     * @param transactions - Transactions from the CSV file
     * @return true if successfully loaded into database, otherwise false
     */
    private boolean importTransactions(List<Transaction> transactions) {

        DynamoDbEnhancedClient ddb = DependencyFactory.dynamoDbClient();
        DynamoDbTable<TransactionDao> table = ddb.table(DYNAMO_TABLE, TableSchema.fromBean(TransactionDao.class));

        logger.debug("Importing Transactions into Database: " + transactions.size());


        int count = 0;
        for(Transaction transaction:transactions) {
            TransactionDao dao = new TransactionDao();
            String pk = USER_PREFIX + ":beckham";
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
    private boolean moveFile(boolean successfulImport) {
        String destinationBucket = (successfulImport) ? config.get("success-bucket") : config.get("error-bucket");

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
                .destinationBucket(destinationBucket)
                .destinationKey(srcKey)
                .build();
        try {
            s3.copyObject(copyObjectRequest);
            s3.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            logger.error("Exception thrown while moving S3 File: " + srcKey);
            e.printStackTrace();
        }

        logger.info("Moved Import File: " + srcKey + " to " + destinationBucket);
        return true;
    }

}
