package io.fouri.budgetchimp.handlers;

import com.opencsv.bean.CsvToBeanBuilder;
import io.fouri.budgetchimp.ChaseTransaction;
import io.fouri.budgetchimp.domain.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

public class ChaseHandler extends TransactionHandler {
    private final Logger logger = LogManager.getLogger(ChaseHandler.class);
    private List<ChaseTransaction> chaseTransactions;

    public ChaseHandler(String srcBucket, String srcKey, String file) {
        super(srcBucket,srcKey, file);
        this.chaseTransactions = null;
    }

    @Override
    /**
     * Purpose in Life:
     * 1. Read Transactions from a Chase specific CSV File
     * 2. Convert Chase Transactions to Generic BudgetChimp Transactions
     * 3. Import Transactions into Database
     * 4. Move file to correct S3 bucket based on success or failure
     * 5. Remain Stateless (aka Handle it All) - Caller should not care or rely on what happens here
     */
    public void handleTransaction() {
        boolean successfulImport = false;
        List<Transaction> transactions = new ArrayList<Transaction>();

        try {
            chaseTransactions = new CsvToBeanBuilder(super.getFileReader())
                    .withType(ChaseTransaction.class).build().parse();

            // Convert Chase Transactions to BudgetChimp Transactions
            for(ChaseTransaction chaseTransaction:chaseTransactions) {
                logger.debug("Processing Transaction: " + chaseTransaction.getDescription());

                transactions.add(chaseTransaction.convertToTransaction());
            }

            // Import to database
            successfulImport = super.importTransactions(transactions);

        } catch (IllegalStateException e) {
            logger.error("Exception thrown: Invalid CSV File: " + getSrcKey());
            e.printStackTrace();
            successfulImport = false;
        } catch (DataFormatException e) {
            logger.error("Exception thrown: Invalid Transaction Date: " + getSrcKey() );
            e.printStackTrace();
            successfulImport = false;
        }

        // Move File accordingly
        if(!RUN_MODE.equals("debug")) {
            super.moveFile(successfulImport);
        }
    }

}
