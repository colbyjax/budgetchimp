package io.fouri.domain;


import java.math.BigDecimal;

public interface Transaction {
     String transactionDate = null;
     String postDate = null;
     String description = null;
     String category = null;
     String type = null;
     BigDecimal amount = null;

     public String getTransactionDate();
     public String getPostDate();
     public String getDescription();
     public String getCategory();
     public String getType();
     public BigDecimal getAmount();

}
