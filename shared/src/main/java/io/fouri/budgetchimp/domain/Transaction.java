package io.fouri.budgetchimp.domain;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {
     String id = null;
     String source = null;
     String transactionDate = null;
     String description = null;
     String category = null;
     String type = null;
     BigDecimal amount = null;
}
