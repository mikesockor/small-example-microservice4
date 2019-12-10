package com.just.example.service;

import com.just.example.exception.ServiceException;
import com.just.example.model.Transaction;
import com.just.example.repository.Repository;
import com.just.example.repository.TransactionRepository;
import com.just.example.utils.Utils;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService() {
        Repository repository = Repository.getRepository(Utils.getStringProperty("repository_type"));
        transactionRepository = repository.getTransactionRepository();
    }

    @GET
    @Path("/{accountId}")
    public Response getAllAccountTransactions(@PathParam("accountId") final long accountId) throws ServiceException {

        final List<Transaction> transactions = transactionRepository.getAllAccountTransactions(accountId);
        if (!transactions.isEmpty()) {
            return Response.status(Response.Status.OK)
                .entity(transactions)
                .build();
        } else {
            return Response.status(Response.Status.NO_CONTENT)
                .entity(transactions)
                .build();
        }

    }

    @POST
    public Response transferFund(Transaction transaction) throws ServiceException {

        final int updateCount = transactionRepository.transferAccountBalance(transaction);
        if (updateCount == 3) {
            return Response.status(Response.Status.OK)
                .build();
        } else {
            // transaction failed
            logger.error("Transaction failed");
            throw new WebApplicationException("Transaction failed", Response.Status.BAD_REQUEST);
        }

    }

}
