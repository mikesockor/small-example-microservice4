package com.just.example.repository;

import com.just.example.exception.ServiceException;
import com.just.example.model.Transaction;
import java.util.List;

public interface TransactionRepository {

    List<Transaction> getAllAccountTransactions(final long accountId) throws ServiceException;

    int transferAccountBalance(final Transaction userTransaction) throws ServiceException;

}
