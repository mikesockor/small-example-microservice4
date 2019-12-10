package com.just.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class Account {

    private long accountId;

    @NotNull
    @JsonProperty(required = true)
    private BigDecimal balance;

    public Account() {
    }

    public Account(final long accountId, @NotNull final BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    @Override public String toString() {
        return "Account{" +
            "accountId='" + accountId + '\'' +
            ", balance=" + balance +
            '}';
    }
}
