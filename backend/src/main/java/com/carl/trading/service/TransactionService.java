package com.carl.trading.service;

import com.carl.trading.mapper.TransactionMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.web.dto.TransactionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionDto> forCustomer(Customer customer) {
        return transactionMapper.findByCustomer(customer.id()).stream().map(TransactionDto::from).toList();
    }
}
