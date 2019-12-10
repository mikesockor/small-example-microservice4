package com.just.example.repository.h2Impl;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import com.just.example.repository.AccountRepository;
import com.just.example.repository.Repository;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;

public class AccountRepositoryH2ImplTest {

    AccountRepository accountRepository;

    @Before
    public void setUp() throws Exception {
        Repository repository = Repository.getRepository(Repository.H2);
        repository.initDB();
        accountRepository = repository.getAccountRepository();
    }

    @Test
    public void getAccountById() throws ServiceException {

        Account account1 = accountRepository.getAccountById(1);
        Account account2 = accountRepository.getAccountById(2);

        assertEquals(0, account1.getBalance()
                .compareTo(new BigDecimal(100000.0000).setScale(4, RoundingMode.HALF_EVEN)));
        assertEquals(0, account2.getBalance()
                .compareTo(new BigDecimal(200000.0000).setScale(4, RoundingMode.HALF_EVEN)));

    }

    @Test
    public void createAccount() throws ServiceException {

        Account argument = new Account(10, new BigDecimal(555.0000));
        long savedId = accountRepository.createAccount(argument);
        Account savedAccount = accountRepository.getAccountById(savedId);

        assertEquals(0, savedAccount.getBalance()
                .compareTo(new BigDecimal(555.0000).setScale(4, RoundingMode.HALF_EVEN)));

    }

    @Test
    public void updateAccountBalanceDeposit() throws ServiceException {

        int operation = accountRepository.updateAccountBalance(3, new BigDecimal(234.456));
        Account savedAccount = accountRepository.getAccountById(3);

        assertEquals(1, operation);
        assertEquals(0, savedAccount.getBalance()
                .compareTo(new BigDecimal(300234.456).setScale(4, RoundingMode.HALF_EVEN)));

    }

    @Test
    public void updateAccountBalanceWithdraw() throws ServiceException {

        int operation = accountRepository.updateAccountBalance(4, new BigDecimal(234.456).negate());
        Account savedAccount = accountRepository.getAccountById(4);

        assertEquals(1, operation);
        assertEquals(0, savedAccount.getBalance()
                .compareTo(new BigDecimal(399765.5440).setScale(4, RoundingMode.HALF_EVEN)));

    }
}