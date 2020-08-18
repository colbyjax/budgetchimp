package io.fouri.budgetchimp;

import com.opencsv.bean.CsvBindByName;
import io.fouri.budgetchimp.domain.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;


@Getter
@Setter
@NoArgsConstructor
public class ChaseTransaction  {
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
    public Transaction convertToTransaction() {
            Transaction transaction = new Transaction();
            transaction.setSource(source);
            transaction.setDescription(this.getDescription());
            transaction.setAmount(this.getAmount());
            transaction.setCategory((!this.getCategory().isEmpty()) ? this.getCategory() : "Unknown");
            transaction.setType(this.getType());
            transaction.setTransactionDate(formatDate(this.getTransactionDate()));
            transaction.setId(generateHash());

            return transaction;
    }


    /**
     * Helper function that takes in the original string of a date and returns a use-able string for NoSQL
     * @param inDate
     * @return
     */
    private static String formatDate(String inDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy", Locale.ENGLISH);
        LocalDate dateTime = LocalDate.parse(inDate, formatter);
        DateTimeFormatter newFormat =  DateTimeFormatter.ofPattern("yyyyMMdd");
        String outDate = newFormat.format(dateTime);
        return outDate;
    }

    /**
     * Generate MD5 hash of Transaction Object to ensure no duplicates
     * @return MD5 Hash representation of Transaction Object
     */
    private String generateHash() {
        StringJoiner hashString = new StringJoiner(":");
        hashString.add(this.source);
        hashString.add(this.description);
        hashString.add(this.transactionDate);
        hashString.add(this.postDate);
        hashString.add(this.amount.toString());
        hashString.add(this.type);

        String hex = new DigestUtils("MD5").digestAsHex(hashString.toString());

        return hex;
    }
}
