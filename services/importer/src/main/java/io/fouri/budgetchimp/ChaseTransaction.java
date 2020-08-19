package io.fouri.budgetchimp;

import com.opencsv.bean.CsvBindByName;
import io.fouri.budgetchimp.domain.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DataFormatException;


@Getter
@Setter
@NoArgsConstructor
public class ChaseTransaction  {
    private final Logger logger = LogManager.getLogger(ChaseTransaction.class);
    private final String source = "CHASE";

    @CsvBindByName(column = "Transaction Date", required = true)
    private String transactionDate;

    @CsvBindByName(column = "Post Date", required = true)
    private String postDate;

    @CsvBindByName(column = "Description", required = true)
    private String description;

    @CsvBindByName(column = "Category", required = false)
    private String category;

    @CsvBindByName(column = "Type", required = true)
    private String type;

    @CsvBindByName(column = "Amount", required = true)
    private BigDecimal amount;

    /**
     * Converts Chase Transaction to BudgetChimp Transaction
     * @return BudgetChimp Transaction
     */
    public Transaction convertToTransaction() throws DataFormatException {
        Transaction transaction = new Transaction();
        transaction.setSource(source);
        transaction.setDescription(this.getDescription());
        transaction.setAmount(this.getAmount());
        transaction.setType(this.getType());
        transaction.setId(generateHash());

        // If Category is empty and type is payment, set category to Payment
        transaction.setCategory((this.getCategory().isEmpty() && this.getType().toLowerCase().equals("payment"))
                ? "Payment" : this.getCategory());

        // Format the date and throw exception if invalid -- log error for debugging
        String formattedDate = formatDate(this.getTransactionDate());
        if(formattedDate == null) {
            logger.error("Unable to process Transaction Date: " + this.transactionDate + " | " + this.toString());
            throw new DataFormatException();
        } else {
            transaction.setTransactionDate(formattedDate);
        }
        return transaction;
    }


    /**
     * Helper function that takes in the original string of a date and returns a use-able string for NoSQL
     * @param inDate
     * @return Formatted String representing date if successful, otherwise null
     */
    public static String formatDate(String inDate) {
        if(inDate == null) {
            return null;
        }

        // Although Chase is currently using m/d/yy currently, want to make function handle a 4 digit year or slightly different formats.
        List<String> formatStrings = Arrays.asList("M/d/yy", "M/d/yyyy", "M/d/y");
        for (String formatString : formatStrings)
        {
            try
            {
                // Try the date input pattern - throws exception if invalid
                Date tryDate = new SimpleDateFormat(formatString).parse(inDate);

                // Define the output pattern
                String pattern = "yyyyMMdd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                String outDate = simpleDateFormat.format(tryDate);

                return outDate;
            }
            catch (ParseException e) {}
        }
        // Should not get here unless it is a bad date
        return null;
    }

    public String toString() {
        StringJoiner objectString = new StringJoiner(":");
        objectString.add(this.source);
        objectString.add(this.description);
        objectString.add(this.transactionDate);
        objectString.add(this.postDate);
        objectString.add(this.amount.toString());
        objectString.add(this.type);

        return objectString.toString();
    }

    /**
     * Generate MD5 hash of Transaction Object to ensure no duplicates
     * @return MD5 Hash representation of Transaction Object
     */
    private String generateHash() {
        String hex = new DigestUtils("MD5").digestAsHex(this.toString());
        return hex;
    }
}
