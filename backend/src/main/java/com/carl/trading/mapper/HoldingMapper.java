package com.carl.trading.mapper;

import com.carl.trading.model.Holding;
import com.carl.trading.web.dto.PortfolioRow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HoldingMapper {

    @Select("SELECT id, customer_id, security_id, quantity, avg_cost_basis_usd "
            + "FROM holdings WHERE customer_id = #{customerId} AND security_id = #{securityId}")
    Holding find(@Param("customerId") long customerId, @Param("securityId") long securityId);

    @Select("SELECT id, customer_id, security_id, quantity, avg_cost_basis_usd "
            + "FROM holdings WHERE customer_id = #{customerId}")
    List<Holding> findByCustomer(long customerId);

    /** Portfolio rows joined to security + exchange for valuation (see HoldingMapper.xml). */
    List<PortfolioRow> findPortfolio(long customerId);

    @Insert("INSERT INTO holdings(customer_id, security_id, quantity, avg_cost_basis_usd) "
            + "VALUES(#{customerId}, #{securityId}, #{quantity}, #{avgCostBasisUsd})")
    void insert(Holding holding);

    @Update("UPDATE holdings SET quantity = #{quantity}, avg_cost_basis_usd = #{avgCostBasisUsd} WHERE id = #{id}")
    void update(Holding holding);

    @Delete("DELETE FROM holdings WHERE id = #{id}")
    void delete(long id);
}
