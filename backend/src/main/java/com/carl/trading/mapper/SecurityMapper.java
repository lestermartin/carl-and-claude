package com.carl.trading.mapper;

import com.carl.trading.model.Security;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SecurityMapper {

    String BASE = "SELECT s.id, s.exchange_id, e.code AS exchange_code, s.symbol, "
            + "s.company_name, s.currency_native, s.snapshot_price_usd "
            + "FROM securities s JOIN exchanges e ON e.id = s.exchange_id ";

    @Select(BASE + "WHERE e.code = #{exchangeCode} ORDER BY s.symbol")
    List<Security> findByExchangeCode(String exchangeCode);

    @Select(BASE + "WHERE s.symbol = #{symbol}")
    Security findBySymbol(String symbol);

    @Select(BASE + "WHERE s.id = #{id}")
    Security findById(long id);

    @Select("SELECT count(*) FROM securities")
    long count();

    @Insert("INSERT INTO securities(exchange_id, symbol, company_name, currency_native, snapshot_price_usd) "
            + "VALUES(#{exchangeId}, #{symbol}, #{companyName}, #{currencyNative}, #{snapshotPriceUsd})")
    void insert(@Param("exchangeId") long exchangeId,
                @Param("symbol") String symbol,
                @Param("companyName") String companyName,
                @Param("currencyNative") String currencyNative,
                @Param("snapshotPriceUsd") java.math.BigDecimal snapshotPriceUsd);
}
