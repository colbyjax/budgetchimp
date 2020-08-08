package io.fouri.budgetchimp.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

import java.math.BigDecimal;

@Data
@DynamoDBTable(tableName="Test1")
public class TransactionDao {
    @DynamoDBHashKey(attributeName="pk")
    private String pk;
    @DynamoDBRangeKey(attributeName = "sk")
    private String sk;
    @DynamoDBAttribute(attributeName="category")
    private String category;
    @DynamoDBAttribute(attributeName="description")
    private String description;
    @DynamoDBAttribute(attributeName="postDate")
    private String postDate;
    @DynamoDBAttribute(attributeName="transactionDate")
    private String transactionDate;
    @DynamoDBAttribute(attributeName="type")
    private String type;
    @DynamoDBAttribute(attributeName="amount")
    private BigDecimal amount;
}
