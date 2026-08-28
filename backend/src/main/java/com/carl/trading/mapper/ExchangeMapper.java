package com.carl.trading.mapper;

import com.carl.trading.model.Exchange;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExchangeMapper {

    @Select("SELECT id, code, name, enabled FROM exchanges WHERE enabled = TRUE ORDER BY code")
    List<Exchange> findEnabled();

    @Select("SELECT id, code, name, enabled FROM exchanges WHERE code = #{code}")
    Exchange findByCode(String code);

    @Select("SELECT count(*) FROM exchanges")
    long count();

    @Insert("INSERT INTO exchanges(code, name, enabled) VALUES(#{code}, #{name}, #{enabled})")
    void insert(Exchange exchange);
}
