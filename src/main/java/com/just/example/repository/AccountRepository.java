package com.just.example.repository;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import java.math.BigDecimal;

public interface AccountRepository {

    long createAccount(final Account account) throws ServiceException;

    Account getAccountById(final long accountId) throws ServiceException;

    int updateAccountBalance(final long accountId, final BigDecimal amount) throws ServiceException;

}
