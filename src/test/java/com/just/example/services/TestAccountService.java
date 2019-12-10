package com.just.example.services;

import com.just.example.model.Account;
import com.just.example.services.TestService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAccountService extends TestService {

    @Test
    public void testGetAccountById() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/1").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(200, statusCode);
        //check the content
        String jsonString = EntityUtils.toString(response.getEntity());
        Account account = mapper.readValue(jsonString, Account.class);
        assertEquals(0, account.getBalance()
                .compareTo(new BigDecimal(100000.0000).setScale(4, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void testGetAccountNotExist() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/99999").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(404, statusCode);
    }

    @Test
    public void testGetAccountBalanceNotExist() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/balance/99999").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(404, statusCode);
    }

    @Test
    public void testGetAccountBalance() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/2/balance").build();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);
        //check the content, assert user test2 have balance 100
        String balance = EntityUtils.toString(response.getEntity());
        BigDecimal res = new BigDecimal(balance).setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal db = new BigDecimal(200000).setScale(4, RoundingMode.HALF_EVEN);
        assertEquals(res, db);
    }

    @Test
    public void testCreateAccount() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/create").build();
        BigDecimal balance = new BigDecimal(555).setScale(4, RoundingMode.HALF_EVEN);
        Account acc = new Account(0L, balance);
        String jsonInString = mapper.writeValueAsString(acc);
        StringEntity entity = new StringEntity(jsonInString);
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setEntity(entity);
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account aAfterCreation = mapper.readValue(jsonString, Account.class);
        assertEquals(0, aAfterCreation.getBalance()
                .compareTo(new BigDecimal(555.0000).setScale(4, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void testDeposit() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/3/deposit/10").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account aAfterCreation = mapper.readValue(jsonString, Account.class);
        assertEquals(0, aAfterCreation.getBalance()
                .compareTo(new BigDecimal(300010.0000).setScale(4, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void testWithdraw() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/4/withdraw/100000").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);
        String jsonString = EntityUtils.toString(response.getEntity());
        Account aAfterCreation = mapper.readValue(jsonString, Account.class);
        assertEquals(0, aAfterCreation.getBalance()
                .compareTo(new BigDecimal(300000.0000).setScale(4, RoundingMode.HALF_EVEN)));
    }

    @Test
    public void testWithdrawNegative() throws IOException, URISyntaxException {

        URI uri = builder.setPath("/account/4/withdraw/-100000").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(400, statusCode);
    }

    @Test
    public void testWithDrawNonSufficientFund() throws IOException, URISyntaxException {
        URI uri = builder.setPath("/account/7/withdraw/10000000.23456").build();
        HttpPut request = new HttpPut(uri);
        request.setHeader("Content-type", "application/json");
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        assertEquals(500, statusCode);
        assertTrue(responseBody.contains("Not sufficient Fund"));
    }


}
