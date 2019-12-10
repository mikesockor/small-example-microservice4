package com.just.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {

    private long transactionId;

    private Timestamp timestamp;

    @JsonProperty(required = true)
    private BigDecimal amount;

    @JsonProperty(required = true)
    private long fromAccountId;

    @JsonProperty(required = true)
    private long toAccountId;

    @JsonProperty(required = true)
    private String purpose;

    public Transaction() {
    }

    public Transaction(final long transactionId, final Timestamp timestamp, final BigDecimal amount,
        final long fromAccountId, final long toAccountId, final String purpose) {
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.purpose = purpose;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final long transactionId) {
        this.transactionId = transactionId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(final long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(final long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    @Override public String toString() {
        return "Transaction{" +
            "transactionId=" + transactionId +
            ", timestamp=" + timestamp +
            ", amount=" + amount +
            ", fromAccountId=" + fromAccountId +
            ", toAccountId=" + toAccountId +
            ", purpose='" + purpose + '\'' +
            '}';
    }

}
