package com.just.example.repository;

import com.just.example.repository.h2Impl.H2Repository;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface Repository {

    BigDecimal zeroAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);

    String H2 = "H2";

    static Repository getRepository(final String type) {

        if (type.equalsIgnoreCase(H2)) {
            return new H2Repository();
        }

        return new H2Repository();

    }

    AccountRepository getAccountRepository();

    TransactionRepository getTransactionRepository();

    void initDB();
}
