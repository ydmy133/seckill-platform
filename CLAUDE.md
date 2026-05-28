# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A full-stack high-concurrency seckill (flash sale) platform — a resume/learning project built step-by-step. The core challenge is preventing overselling when 100 items are contested by thousands of concurrent users.

## Development Commands

All backend commands run from `backend/`:

```bash
# Build (uses Maven Wrapper — no local Maven needed)
./mvnw compile
./mvnw package

# Run the Spring Boot app
./mvnw spring-boot:run

# Run tests
./mvnw test
```

Infrastructure (MySQL, Redis, RabbitMQ) must be running locally before starting the app. See `application.yml` for connection defaults (MySQL on 3306, Redis on 6379, RabbitMQ on 5672).

**Database initialization:**
```bash
mysql -u root -p < sql/init.sql
```

**Frontend** (not yet scaffolded):
```bash
cd frontend && npm install && npm run dev
```

## Architecture

```
Browser (Vue 3)
    │
    ├── Static files ──▶ Nginx (80)
    │
    └── API (/api/*) ──▶ Spring Boot (8080)
                             │
              ┌──────────────┼──────────────┬──────────────┐
              │              │              │              │
           Redis          MySQL        RabbitMQ       Lua scripts
         (stock/limiting) (user/product/ (async orders) (resources/lua/)
                           order)
```

**Seckill request flow (the key path):**

1. RateLimitInterceptor — Redis Lua token-bucket check
2. SeckillController — validates captcha token
3. SeckillService — executes Redis Lua script (atomic: check stock + check repeat + decrement + mark user)
4. If success → `RabbitTemplate.convertAndSend()` to RabbitMQ, immediately return to client
5. `@RabbitListener` consumer (async) — creates order in MySQL with optimistic locking
6. Frontend polls `GET /api/seckill/{id}/result/{userId}` every 500ms for the outcome

**Three-layer anti-overselling defense:**
- Layer 1: Redis Lua atomic script (handles ~99% of requests)
- Layer 2: MySQL unique index `uk_user_seckill(user_id, seckill_product_id)` (duplicate prevention)
- Layer 3: MySQL optimistic locking via `version` column (prevents negative stock)

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Backend | Spring Boot 3.2, MyBatis-Plus 3.5, JDK 17 |
| Auth | JJWT 0.12 (HMAC-SHA256), BCrypt via spring-security-crypto |
| Cache | Redis with Lettuce client |
| Queue | RabbitMQ (Spring AMQP, manual ACK) |
| Frontend | Vue 3 + Vite + Element Plus + Axios + Pinia + Vue Router 4 |
| DB | MySQL 8.0 |

## Package Structure (planned by README)

```
com.seckill
├── SeckillApplication.java
├── common/          — JwtUtils, RedisKeyPrefix constants
├── config/          — RedisConfig, RabbitMQConfig, WebConfig, BeanConfig, LuaScriptConfig, MyBatisPlusConfig
├── consumer/        — RabbitMQ @RabbitListener consumers
├── controller/      — REST controllers (Auth, Product, AdminProduct, Seckill, Order)
├── dto/             — Request/response DTOs
├── entity/          — MyBatis-Plus entities (User, Product, SeckillProduct, Order)
├── exception/       — BusinessException, GlobalExceptionHandler
├── interceptor/     — JwtInterceptor, RateLimitInterceptor
├── mapper/          — MyBatis-Plus BaseMapper interfaces
├── service/         — Service interfaces + impl/
└── vo/              — Result<T> unified response wrapper
```

## Current State

Only the project scaffold exists — `pom.xml` with all dependencies, `SeckillApplication.java`, `application.yml` (fully configured), and `sql/init.sql` (4 tables: user, product, seckill_product, seckill_order). No business code has been implemented yet. The README.md is a tutorial document with all planned code inline — follow its steps sequentially to build out the platform.

## Key Design Decisions

- **Redis Lua for atomicity** — EVALSHA runs "check stock + check repeat + decrement + mark user" as one atomic operation in Redis's single-threaded event loop
- **RabbitMQ over Redis Streams** — durable queues, dead-letter exchange support, manual ACK; the consumer creates orders asynchronously so the seckill endpoint returns in <10ms without touching MySQL
- **JWT over Session** — stateless auth for horizontal scaling; no server-side session sync needed
- **MyBatis-Plus over JPA** — LambdaQueryWrapper for compile-safe field references; full SQL control for `SELECT ... FOR UPDATE` and optimistic lock UPDATE
- **Price snapshot in orders** — `seckill_order.seckill_price` stores the price at purchase time; never JOIN to product table for historical prices (financial compliance)
