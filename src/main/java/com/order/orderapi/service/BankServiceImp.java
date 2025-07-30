package com.order.orderapi.service;

import com.order.orderapi.dto.TransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceImp {

    private final RestTemplate restTemplate;
    private final String depositUrl = "http://localhost:8081/api/transactions/deposit";
    private final String withdrawUrl = "http://localhost:8081/api/transactions/withdraw";


    public ResponseEntity<String> deposit(TransactionRequest transactionRequest) {
        return restTemplate.postForEntity(depositUrl, transactionRequest, String.class);
    }


    public ResponseEntity<String> withdraw(TransactionRequest transactionRequest) {
        return restTemplate.postForEntity(withdrawUrl, transactionRequest, String.class);
    }
}