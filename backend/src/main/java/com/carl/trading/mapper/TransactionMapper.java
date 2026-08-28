package com.carl.trading.mapper;

import com.carl.trading.model.TransactionRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transactions("
            + "customer_id, security_id, side, order_type, status, quantity, "
            + "limit_price_usd, executed_price_usd, cash_delta_usd, reason, created_at) VALUES ("
            + "#{customerId}, #{securityId}, #{side}, #{orderType}, #{status}, #{quantity}, "
            + "#{limitPriceUsd}, #{executedPriceUsd}, #{cashDeltaUsd}, #{reason}, #{createdAt})")
    void insert(TransactionRecord tx);

    /** Full log for a customer, newest first, joined to security + exchange (see TransactionMapper.xml). */
    List<TransactionRecord> findByCustomer(long customerId);
}
