package com.just.example.services;


import com.just.example.model.Transaction;
import com.just.example.model.TransactionPurpose;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.assertEquals;


public class TestTransactionService extends TestService {

    @Test
    public void testTransactionEnoughFund() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/transaction").build();

        Transaction transaction = new Transaction(
                0L,
                Timestamp.from(Instant.now()),
                new BigDecimal(1050).setScale(4, RoundingMode.HALF_EVEN),
                10L,
                11L,
                TransactionPurpose.TRANSFER.name()
        );

        String jsonInString = mapper.writeValueAsString(transaction);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void testTransactionNotEnoughFund() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/transaction").build();

        Transaction transaction = new Transaction(
                0L,
                Timestamp.from(Instant.now()),
                new BigDecimal(105000).setScale(4, RoundingMode.HALF_EVEN),
                11L,
                10L,
                TransactionPurpose.TRANSFER.name()
        );

        String jsonInString = mapper.writeValueAsString(transaction);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(500, statusCode);
    }

}
