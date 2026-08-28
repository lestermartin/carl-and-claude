# carl-and-claude

Carl needs brokerage self-service tools. He is NOT a programmer, but he heard about "vibe coding" with Claude and he gave it a go! Read more about their journey [here](./HowItStarted.md).

![Carl](https://github.com/lestermartin/carl-and-claude/blob/main/CarlWithCup.jpg?raw=true "Carl")

This repo holds a small, self-contained **self-service brokerage demo application**:

| Tier | Tech | Container port | Host port |
|------|------|----------------|-----------|
| Front-end | Angular 20 (served by nginx) | 80 | **8088** |
| REST API | Java 21 + Spring Boot 3.4 + MyBatis | 8080 | **6432** |
| Database | PostgreSQL 16 | 5432 | **5432** |

```
browser ──▶ http://localhost:8088 ──▶ nginx ──┬── static Angular bundle
                                              └── /api/* ──▶ backend:8080 ──▶ postgres:5432
```

The whole stack runs locally with `docker compose`. Nothing calls the public internet at
runtime.

---

## Quick start

```bash
cp .env.example .env
docker compose up --build
```

First build takes a few minutes (Maven + npm downloads). When it settles:

* Web app: <http://localhost:8088>
* REST API: <http://localhost:6432/api> (health at <http://localhost:6432/actuator/health>)
* PostgreSQL: `localhost:5432`, database `tradingapp`

Stop it:

```bash
docker compose down          # keep the database volume
docker compose down -v       # also wipe the database (next start re-seeds from scratch)
```

---

## Demo accounts

Nine users are seeded: **`customer1` … `customer9`**, all with password **`cu$tP@$$w0rd`**.

Each customer gets, generated deterministically (fixed RNG seed, so every fresh database is
identical):

* a random US identity — first/last name, `###-##-####` tax ID, USA street address
* a **$40,000.00** cash balance
* **3–10 holdings** chosen from the tradable universe
* a matching **backdated BUY history** (random dates within the last 3 years, each purchase
  $20k–$40k, priced by a synthetic back-cast from the current snapshot price)

There is no sign-up flow — the nine accounts are the whole user list for this version.

---

## What you can do

| Page | Notes |
|------|-------|
| **Login / Logout** | JWT is stored in the browser; logout just discards it. |
| **Home** | Holdings summary: quantity, average cost, current price, market value, unrealized P/L, plus cash and total account value. Each row has a **Sell** button that opens the Trade page pre-filled for that position. |
| **Trade** | Buy/sell, `MARKET` or `LIMIT`. Pick an exchange, then a security from its list. |
| **Transactions** | Full log, newest first, including rejected orders. |
| **Profile** | Edit any field **except** username and password. |

### Order rules (intentionally minimal)

* **MARKET** orders execute immediately at the current snapshot price.
* **LIMIT** orders execute at the snapshot price **if the limit is favorable**
  (buy limit ≥ market, or sell limit ≤ market). Otherwise the order is **rejected and still
  written to the transaction log** — there is no resting-order / matching engine.
* The only business checks are **enough cash to buy** and **enough shares to sell**. A failed
  check produces a `REJECTED` transaction (the API returns `200` with `status: "REJECTED"`).

### Exchanges & market data

Exchanges are a configurable table, seeded with **Nasdaq, NYSE, Shanghai (SSE), and London
(LSE)**. For each, `backend/src/main/resources/marketdata/*.csv` holds a representative list
of well-known constituents (~40 each) with a recent **USD** price snapshot.

**Single-currency simplification:** every security stores a pre-converted USD price, so
Shanghai/London stocks are bought with the USD cash balance and there is no FX handling.
Prices are static — the snapshot price *is* "the current price" everywhere.

To use real prices later, replace the CSVs (same columns:
`symbol,company_name,currency_native,snapshot_price_usd`) and `docker compose down -v && up`.

---

## Connecting to PostgreSQL

```bash
psql "host=localhost port=5432 dbname=tradingapp user=db-user password=db-p@\$\$w0rd"
```

Tables: `exchanges`, `securities`, `customers`, `holdings`, `transactions`.

---

## Running the tiers natively (for development)

You need JDK 21, Maven, Node 20, and a local PostgreSQL matching `.env`.

**Backend**

```bash
cd backend
./mvnw spring-boot:run        # http://localhost:8080  (expects postgres on localhost:5432)
./mvnw test                   # unit tests (OrderService, PortfolioService, DataSeeder)
```

**Frontend**

```bash
cd frontend
npm install
npm start                     # http://localhost:4200, proxies /api to http://localhost:6432
```

> `npm start` proxies to port **6432** (the Docker backend). If you run the backend natively
> on 8080, edit `frontend/proxy.conf.json`.

---

## Layout

```
backend/    Spring Boot API
  src/main/java/com/carl/trading/
    web/         REST controllers + DTOs
    service/     AuthService, ProfileService, PortfolioService, OrderService, MarketService
    mapper/      MyBatis mapper interfaces
    seed/        DataSeeder (reference data + demo customers)
    security/    JWT filter + helpers
  src/main/resources/
    db/migration/V1__init.sql     Flyway schema
    mybatis/*.xml                 join queries
    marketdata/*.csv              seeded securities + prices
    seed/*                        name / street / city lists
frontend/   Angular 20 app (standalone components, signals)
docker-compose.yml
.env.example
```

---

## Configuration reference

| Variable | Default | Used by |
|----------|---------|---------|
| `POSTGRES_DB` | `tradingapp` | postgres, backend |
| `POSTGRES_USER` | `db-user` | postgres, backend |
| `POSTGRES_PASSWORD` | `db-p@$$w0rd` | postgres, backend |
| `APP_JWT_SECRET` | (dev string in `.env.example`) | backend — must be ≥ 32 chars |
| `APP_SEED_ENABLED` | `true` | backend — set `false` to skip demo-data seeding |
