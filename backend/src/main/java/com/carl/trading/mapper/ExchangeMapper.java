package com.carl.trading.mapper;

import com.carl.trading.model.Exchange;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExchangeMapper {

    String COLS = "id, code, name, enabled, time_zone, open_local, close_local, open_days";

    @Select("SELECT " + COLS + " FROM exchanges WHERE enabled = TRUE ORDER BY code")
    List<Exchange> findEnabled();

    @Select("SELECT " + COLS + " FROM exchanges WHERE code = #{code}")
    Exchange findByCode(String code);

    @Select("SELECT count(*) FROM exchanges")
    long count();

    @Insert("INSERT INTO exchanges(code, name, enabled, time_zone, open_local, close_local, open_days) "
            + "VALUES(#{code}, #{name}, #{enabled}, #{timeZone}, #{openLocal}, #{closeLocal}, #{openDays})")
    void insert(Exchange exchange);
}
