package com.carl.trading;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.carl.trading.mapper")
public class TradingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingAppApplication.class, args);
    }
}
