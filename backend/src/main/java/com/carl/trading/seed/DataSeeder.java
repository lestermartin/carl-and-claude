package com.carl.trading.seed;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.mapper.ExchangeMapper;
import com.carl.trading.mapper.HoldingMapper;
import com.carl.trading.mapper.SecurityMapper;
import com.carl.trading.mapper.TransactionMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.model.Exchange;
import com.carl.trading.model.Holding;
import com.carl.trading.model.Security;
import com.carl.trading.model.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Populates reference data (exchanges + securities) and, on an empty database, 9 demo customers
 * each with a randomized identity, a $40,000 cash balance, 3-10 holdings, and a matching backdated
 * BUY history. Uses a fixed RNG seed so every fresh database looks identical.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final long RNG_SEED = 42L;
    private static final int CUSTOMER_COUNT = 9;
    private static final String SHARED_PASSWORD = "cu$tP@$$w0rd";
    private static final BigDecimal STARTING_CASH = new BigDecimal("40000.00");
    private static final int MIN_HOLDINGS = 3;
    private static final int MAX_HOLDINGS = 10;
    private static final int MIN_SPEND = 20_000;
    private static final int MAX_SPEND = 40_000;
    private static final int HISTORY_DAYS = 3 * 365;

    private static final List<Exchange> EXCHANGES = List.of(
            new Exchange(null, "NASDAQ", "Nasdaq Stock Market", true),
            new Exchange(null, "NYSE", "New York Stock Exchange", true),
            new Exchange(null, "SSE", "Shanghai Stock Exchange", true),
            new Exchange(null, "LSE", "London Stock Exchange", true));

    private final ExchangeMapper exchangeMapper;
    private final SecurityMapper securityMapper;
    private final CustomerMapper customerMapper;
    private final HoldingMapper holdingMapper;
    private final TransactionMapper transactionMapper;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DataSeeder(ExchangeMapper exchangeMapper, SecurityMapper securityMapper,
                      CustomerMapper customerMapper, HoldingMapper holdingMapper,
                      TransactionMapper transactionMapper, PasswordEncoder passwordEncoder,
                      @org.springframework.beans.factory.annotation.Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.exchangeMapper = exchangeMapper;
        this.securityMapper = securityMapper;
        this.customerMapper = customerMapper;
        this.holdingMapper = holdingMapper;
        this.transactionMapper = transactionMapper;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Seeding disabled (app.seed.enabled=false)");
            return;
        }
        seedExchanges();
        seedSecurities();
        seedCustomers();
    }

    private void seedExchanges() {
        if (exchangeMapper.count() > 0) {
            return;
        }
        EXCHANGES.forEach(exchangeMapper::insert);
        log.info("Seeded {} exchanges", EXCHANGES.size());
    }

    private void seedSecurities() {
        if (securityMapper.count() > 0) {
            return;
        }
        int total = 0;
        for (Exchange listed : EXCHANGES) {
            Exchange exchange = exchangeMapper.findByCode(listed.code());
            for (String[] row : readCsv("marketdata/" + listed.code().toLowerCase() + ".csv")) {
                securityMapper.insert(exchange.id(), row[0], row[1], row[2], new BigDecimal(row[3]));
                total++;
            }
        }
        log.info("Seeded {} securities across {} exchanges", total, EXCHANGES.size());
    }

    private void seedCustomers() {
        if (customerMapper.count() > 0) {
            log.info("Customers already present - skipping demo customer seeding");
            return;
        }

        Random rng = new Random(RNG_SEED);
        List<String> firstNames = readLines("seed/first-names.txt");
        List<String> lastNames = readLines("seed/last-names.txt");
        List<String> streets = readLines("seed/street-names.txt");
        List<String[]> cities = readCsv("seed/cities.csv");

        List<Security> universe = new ArrayList<>();
        for (Exchange listed : EXCHANGES) {
            universe.addAll(securityMapper.findByExchangeCode(listed.code()));
        }

        String passwordHash = passwordEncoder.encode(SHARED_PASSWORD);
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 1; i <= CUSTOMER_COUNT; i++) {
            String username = "customer" + i;
            String first = pick(rng, firstNames);
            String last = pick(rng, lastNames);
            String taxId = String.format("%03d-%02d-%04d",
                    100 + rng.nextInt(900), 10 + rng.nextInt(90), 1000 + rng.nextInt(9000));
            String[] city = pick(rng, cities);
            String line1 = (100 + rng.nextInt(9900)) + " " + pick(rng, streets);
            String line2 = rng.nextInt(3) == 0 ? "Apt " + (1 + rng.nextInt(40)) : null;

            customerMapper.insert(new Customer(null, username, passwordHash, first, last, taxId,
                    line1, line2, city[0], city[1], city[2], STARTING_CASH));
            long customerId = customerMapper.findByUsername(username).id();

            int holdingCount = MIN_HOLDINGS + rng.nextInt(MAX_HOLDINGS - MIN_HOLDINGS + 1);
            List<Security> shuffled = new ArrayList<>(universe);
            java.util.Collections.shuffle(shuffled, rng);

            for (Security security : shuffled.subList(0, holdingCount)) {
                int daysAgo = 1 + rng.nextInt(HISTORY_DAYS);
                OffsetDateTime purchasedAt = now.minusDays(daysAgo).minusMinutes(rng.nextInt(480));

                BigDecimal historicalPrice = backcastPrice(rng, security.snapshotPriceUsd(), daysAgo);
                int targetSpend = MIN_SPEND + rng.nextInt(MAX_SPEND - MIN_SPEND + 1);
                long quantity = Math.max(1L, BigDecimal.valueOf(targetSpend)
                        .divide(historicalPrice, 0, RoundingMode.HALF_UP).longValue());
                BigDecimal cost = historicalPrice.multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);

                holdingMapper.insert(new Holding(null, customerId, security.id(), quantity, historicalPrice));
                transactionMapper.insert(new TransactionRecord(
                        null, customerId, security.id(), security.symbol(), security.exchangeCode(),
                        "BUY", "MARKET", "FILLED", quantity, null, historicalPrice,
                        cost.negate(), "Seeded opening position", purchasedAt));
            }
        }
        log.info("Seeded {} demo customers (username customer1..customer{}, password '{}')",
                CUSTOMER_COUNT, CUSTOMER_COUNT, SHARED_PASSWORD);
    }

    /** Random-walk the current price backwards; deterministic given {@code rng}. */
    private static BigDecimal backcastPrice(Random rng, BigDecimal snapshot, int daysAgo) {
        double years = daysAgo / 365.0;
        double factor = 1.0 + rng.nextGaussian() * 0.22 * Math.sqrt(years) - 0.04 * years;
        factor = Math.max(0.4, Math.min(1.8, factor));
        BigDecimal price = snapshot.multiply(BigDecimal.valueOf(factor)).setScale(4, RoundingMode.HALF_UP);
        return price.signum() <= 0 ? new BigDecimal("0.0100") : price;
    }

    private static <T> T pick(Random rng, List<T> list) {
        return list.get(rng.nextInt(list.size()));
    }

    private static List<String> readLines(String path) {
        try (BufferedReader reader = reader(path)) {
            List<String> out = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    out.add(trimmed);
                }
            }
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading " + path, e);
        }
    }

    /** Reads a simple comma-separated resource, skipping a header row and any blank/# lines. */
    private static List<String[]> readCsv(String path) {
        List<String> lines = readLines(path);
        List<String[]> out = new ArrayList<>();
        boolean header = true;
        for (String line : lines) {
            if (header) {
                header = false;
                continue;
            }
            String[] parts = line.split(",", -1);
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            out.add(parts);
        }
        return out;
    }

    private static BufferedReader reader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8));
    }
}
