package com.just.example.service;

import com.just.example.exception.ServiceException;
import com.just.example.model.Account;
import com.just.example.repository.AccountRepository;
import com.just.example.repository.Repository;
import com.just.example.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService() {
        Repository repository = Repository.getRepository(Utils.getStringProperty("repository_type"));
        accountRepository = repository.getAccountRepository();
    }

    @GET
    @Path("/{accountId}")
    public Account getAccount(@PathParam("accountId") final long accountId) throws ServiceException {
        final Account account = accountRepository.getAccountById(accountId);
        if (account == null) {
            throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
        }
        return account;
    }

    @GET
    @Path("/{accountId}/balance")
    public BigDecimal getBalance(@PathParam("accountId") final long accountId) throws ServiceException {

        final Account account = accountRepository.getAccountById(accountId);
        if (account == null) {
            throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
        }
        return account.getBalance();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Account createAccount(@Valid final Account account) throws ServiceException {
        final long accountId = accountRepository.createAccount(account);
        return accountRepository.getAccountById(accountId);
    }

    @PUT
    @Path("/{accountId}/deposit/{amount}")
    public Account deposit(@PathParam("accountId") final long accountId, @PathParam("amount") final BigDecimal amount) throws ServiceException {

        if (amount.compareTo(Repository.zeroAmount) <= 0) {
            throw new WebApplicationException("Invalid Deposit amount", Response.Status.BAD_REQUEST);
        }

        accountRepository.updateAccountBalance(accountId, amount.setScale(4, RoundingMode.HALF_EVEN));
        return accountRepository.getAccountById(accountId);
    }

    @PUT
    @Path("/{accountId}/withdraw/{amount}")
    public Account withdraw(@PathParam("accountId") final long accountId, @PathParam("amount") final BigDecimal amount) throws ServiceException {

        if (amount.compareTo(Repository.zeroAmount) <= 0) {
            throw new WebApplicationException("Invalid Deposit amount", Response.Status.BAD_REQUEST);
        }
        final BigDecimal delta = amount.negate();
        logger.atDebug()
                .addArgument(delta)
                .addArgument(accountId)
                .log("Withdraw service: delta change to account  {} Account ID = {}");
        accountRepository.updateAccountBalance(accountId, delta.setScale(4, RoundingMode.HALF_EVEN));
        return accountRepository.getAccountById(accountId);
    }

}
