package io.fouri.domain;

import com.opencsv.bean.CsvBindByName;
import io.fouri.domain.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
public class ChaseTransaction implements Transaction {

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
}
