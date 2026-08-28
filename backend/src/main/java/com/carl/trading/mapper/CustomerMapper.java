package com.carl.trading.mapper;

import com.carl.trading.model.Customer;
import com.carl.trading.web.dto.UpdateProfileRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface CustomerMapper {

    String COLS = "id, username, password_hash, first_name, last_name, tax_id, "
            + "address_line1, address_line2, city, state, postal_code, cash_balance_usd";

    @Select("SELECT " + COLS + " FROM customers WHERE username = #{username}")
    Customer findByUsername(String username);

    @Select("SELECT " + COLS + " FROM customers WHERE id = #{id}")
    Customer findById(long id);

    @Select("SELECT count(*) FROM customers")
    long count();

    @org.apache.ibatis.annotations.Insert("INSERT INTO customers("
            + "username, password_hash, first_name, last_name, tax_id, "
            + "address_line1, address_line2, city, state, postal_code, cash_balance_usd) VALUES ("
            + "#{username}, #{passwordHash}, #{firstName}, #{lastName}, #{taxId}, "
            + "#{addressLine1}, #{addressLine2}, #{city}, #{state}, #{postalCode}, #{cashBalanceUsd})")
    void insert(Customer customer);

    @Update("UPDATE customers SET cash_balance_usd = #{cash} WHERE id = #{id}")
    void updateCash(@Param("id") long id, @Param("cash") BigDecimal cash);

    @Update("UPDATE customers SET "
            + "first_name = #{p.firstName}, last_name = #{p.lastName}, tax_id = #{p.taxId}, "
            + "address_line1 = #{p.addressLine1}, address_line2 = #{p.addressLine2}, "
            + "city = #{p.city}, state = #{p.state}, postal_code = #{p.postalCode} "
            + "WHERE id = #{id}")
    void updateProfile(@Param("id") long id, @Param("p") UpdateProfileRequest p);
}
