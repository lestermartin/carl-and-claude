package com.carl.trading.seed;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.mapper.ExchangeMapper;
import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.mapper.SecurityMapper;
import com.carl.trading.mapper.TransactionMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.model.Holding;
import com.carl.trading.model.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:seedtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false"
})
@Import({DataSeeder.class, DataSeederTest.TestBeans.class})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DataSeederTest {

    @TestConfiguration
    static class TestBeans {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    DataSeeder dataSeeder;
    @org.springframework.beans.factory.annotation.Autowired
    ExchangeMapper exchangeMapper;
    @org.springframework.beans.factory.annotation.Autowired
    SecurityMapper securityMapper;
    @org.springframework.beans.factory.annotation.Autowired
    CustomerMapper customerMapper;
    @org.springframework.beans.factory.annotation.Autowired
    HoldingMapper holdingMapper;
    @org.springframework.beans.factory.annotation.Autowired
    TransactionMapper transactionMapper;
    @org.springframework.beans.factory.annotation.Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void seed() {
        dataSeeder.run(null);
    }

    @Test
    void seedsFourExchangesAndTheFullSecurityUniverse() {
        assertThat(exchangeMapper.count()).isEqualTo(4);
        assertThat(exchangeMapper.findEnabled()).extracting("code")
                .containsExactlyInAnyOrder("NASDAQ", "NYSE", "SSE", "LSE");
        assertThat(securityMapper.count()).isEqualTo(175);

        assertThat(exchangeMapper.findEnabled()).allSatisfy(ex -> {
            assertThat(ex.timeZone()).isNotBlank();
            assertThat(ex.openLocal()).isNotNull();
            assertThat(ex.closeLocal()).isNotNull();
            assertThat(ex.openDays()).isEqualTo("MON,TUE,WED,THU,FRI");
        });
    }

    @Test
    void seedsNineCustomersEachWith40kCashAnd3To10Holdings() {
        assertThat(customerMapper.count()).isEqualTo(9);

        for (int i = 1; i <= 9; i++) {
            Customer c = customerMapper.findByUsername("customer" + i);
            assertThat(c).as("customer%d exists", i).isNotNull();
            assertThat(c.cashBalanceUsd()).isEqualByComparingTo("40000.00");
            assertThat(c.taxId()).matches("\\d{3}-\\d{2}-\\d{4}");
            assertThat(passwordEncoder.matches("cu$tP@$$w0rd", c.passwordHash())).isTrue();

            List<Holding> holdings = holdingMapper.findByCustomer(c.id());
            assertThat(holdings.size()).as("customer%d holding count", i).isBetween(3, 10);

            List<TransactionRecord> log = transactionMapper.findByCustomer(c.id());
            assertThat(log).hasSameSizeAs(holdings);
            assertThat(log).allSatisfy(t -> {
                assertThat(t.side()).isEqualTo("BUY");
                assertThat(t.status()).isEqualTo("FILLED");
                assertThat(t.cashDeltaUsd()).isLessThan(BigDecimal.ZERO);
            });
        }
    }

    @Test
    void isIdempotent_secondRunAddsNothing() {
        dataSeeder.run(null);
        assertThat(customerMapper.count()).isEqualTo(9);
        assertThat(securityMapper.count()).isEqualTo(175);
    }
}
