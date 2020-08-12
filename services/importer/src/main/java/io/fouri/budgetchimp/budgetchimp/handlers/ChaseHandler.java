package io.fouri.budgetchimp.budgetchimp.handlers;

import com.opencsv.bean.CsvToBeanBuilder;
import io.fouri.budgetchimp.ChaseTransaction;
import io.fouri.budgetchimp.domain.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ChaseHandler extends TransactionHandler {
    private final Logger logger = LogManager.getLogger(ChaseHandler.class);
    private List<ChaseTransaction> chaseTransactions;

    public ChaseHandler(String srcBucket, String srcKey) {
        super(srcBucket,srcKey);
        this.chaseTransactions = null;
    }

    @Override
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
            super.processTransactions(transactions);

        } catch (IllegalStateException e) {
            logger.error("Exception thrown: Invalid CSV File: " + getSrcKey());
            e.printStackTrace();
            successfulImport = false;
        }

    }


}
