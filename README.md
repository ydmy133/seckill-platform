# 秒杀平台 (Seckill Platform) — 全栈练手项目

> 本 README 是一份**手把手教程**，带你从零构建一个高并发秒杀平台。
> 每一步都说明：**做什么 → 写什么代码 → 为什么这么写 → 如何验证**。
> 照着一行一行敲，你会深入理解 Spring Boot、Redis、JWT、MyBatis-Plus、Vue3 的实际运用。

---

## 项目概述

**目标**：构建一个支持高并发的秒杀/抢购平台，解决"100 件库存被 10 万人抢"场景下的超卖问题。

**核心亮点**（面试时要能讲清楚）：

| 问题 | 解决方案 |
|------|----------|
| Redis 与 DB 之间的库存一致性 | 三层防超卖：Lua 原子扣减 → DB 唯一索引 → DB 乐观锁 |
| 瞬时流量冲击数据库 | RabbitMQ 消息队列异步削峰，秒杀响应不等待 DB 写入 |
| 黄牛脚本刷接口 | 滑块验证码 + 令牌桶限流 |
| 一人抢多次 | Redis Set 记录已购用户 + DB 唯一约束 |

---

## 目录

- [环境准备](#环境准备)
- [技术栈全解析（面试必背）](#技术栈全解析面试必背)
- [架构总览](#架构总览)
- [阶段一：项目脚手架](#阶段一项目脚手架)
  - [Step 1: 创建 Spring Boot 项目](#step-1-创建-spring-boot-项目)
  - [Step 2: 配置数据库连接](#step-2-配置数据库连接)
  - [Step 3: 建表 + 实体类 + Mapper](#step-3-建表--实体类--mapper)
- [阶段二：用户认证](#阶段二用户认证)
  - [Step 4: 用户注册（BCrypt 密码加密）](#step-4-用户注册bcrypt-密码加密)
  - [Step 5: 登录 + JWT + 拦截器](#step-5-登录--jwt--拦截器)
- [阶段三：商品管理](#阶段三商品管理)
  - [Step 6: 商品 CRUD（管理员）](#step-6-商品-crud管理员)
  - [Step 7: 秒杀商品配置](#step-7-秒杀商品配置)
- [阶段四：Redis 集成](#阶段四redis-集成)
  - [Step 8: 安装 Redis + 配置连接](#step-8-安装-redis--配置连接)
  - [Step 9: Redis Key 设计 + 基础操作验证](#step-9-redis-key-设计--基础操作验证)
- [阶段五：秒杀核心引擎（最关键）](#阶段五秒杀核心引擎最关键)
  - [Step 10: 编写 Lua 原子扣库存脚本](#step-10-编写-lua-原子扣库存脚本)
  - [Step 11: 库存预热服务](#step-11-库存预热服务)
  - [Step 12: 秒杀执行逻辑](#step-12-秒杀执行逻辑)
  - [Step 13: 异步订单消费者（RabbitMQ）](#step-13-异步订单消费者rabbitmq)
- [阶段六：防护机制](#阶段六防护机制)
  - [Step 14: 令牌桶限流](#step-14-令牌桶限流)
  - [Step 15: 乐观锁 + 唯一索引兜底](#step-15-乐观锁--唯一索引兜底)
- [阶段七：前端开发](#阶段七前端开发)
  - [Step 16: 脚手架 Vue3 项目](#step-16-脚手架-vue3-项目)
  - [Step 17: 登录注册页 + Axios JWT 拦截器](#step-17-登录注册页--axios-jwt-拦截器)
  - [Step 18: 商品列表页](#step-18-商品列表页)
  - [Step 19: 秒杀详情页（核心前端页）](#step-19-秒杀详情页核心前端页)
  - [Step 20: 订单列表页 + 结果轮询](#step-20-订单列表页--结果轮询)
- [阶段八：集成与测试](#阶段八集成与测试)
  - [Step 21: Nginx 动静分离](#step-21-nginx-动静分离)
  - [Step 22: JMeter 压测验证](#step-22-jmeter-压测验证)
  - [Step 23: 异常处理完善](#step-23-异常处理完善)
- [面试问答 20 题](#面试问答-20-题)

---

## 环境准备

开始之前，确保你本地装了这些：

| 工具 | 版本要求 | 验证命令 |
|------|----------|----------|
| JDK | 17+ | `java --version` |
| Maven | 通过 Maven Wrapper 自动下载，无需手动安装 | `cd backend && ./mvnw --version` |
| MySQL | 8.0+ | `mysql -u root -p` |
| Redis | 6.0+ | `redis-cli ping` |
| RabbitMQ | 3.12+ | 见下方说明 (需先装 Erlang) |
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |
| Nginx | 1.24+ | 见 Step 21，非必需（可最后再装）|

**RabbitMQ 安装 (Linux)**：

RabbitMQ 依赖 Erlang 运行时：

```bash
# 1. 安装 Erlang 和 RabbitMQ
sudo apt install -y erlang rabbitmq-server

# 2. 启动服务
sudo systemctl start rabbitmq-server

# 3. 启用管理插件（可视化 Web 控制台）
sudo rabbitmq-plugins enable rabbitmq_management

# 4. 设置开机自启
sudo systemctl enable rabbitmq-server
```

访问管理后台：http://localhost:15672 （默认用户名/密码：guest/guest）

Spring Boot 连接 RabbitMQ 默认端口 5672，管理后台 15672。

**初始化数据库**：
```bash
# 使用项目中的 init.sql
mysql -u root -p123123 < /home/ydmy/seckill-platform/sql/init.sql
```

---

## 技术栈全解析（面试必背）

### 为什么用 Spring Boot 而不是 Node.js/Go？

**面试回答**：
> Spring Boot 的自动配置消除了大量样板代码，内嵌 Tomcat 默认支持 200+ 并发连接。Java 生态的监控体系（Actuator + Micrometer）成熟，方便排查生产问题。在国内互联网公司，Spring Boot + MyBatis-Plus 是绝对的主流组合，团队招聘和维护成本更低。

### 为什么用 MyBatis-Plus 而不是 JPA/Hibernate？

**面试回答**：
> MyBatis-Plus 提供了 LambdaQueryWrapper，避免硬编码字段名，编译期就能发现拼写错误。相比 JPA 的自动 SQL 生成，MyBatis-Plus 对复杂查询保持完全 SQL 可控性——我可以在秒杀场景精确控制行锁（`SELECT ... FOR UPDATE`），而不用猜 Hibernate 会生成什么样的 JOIN。内置的分页插件和乐观锁插件只需一个 `@Bean` 配置。

### 为什么用 Redis 做库存扣减？

**面试回答**：
> Redis 有两个核心优势：一是单线程事件循环模型，天然串行化所有命令，不需要加锁；二是内存操作，单机轻松 10 万 QPS。更重要的是，Redis 支持 Lua 脚本，可以把"查库存 + 扣库存 + 查重复 + 标记用户"四步操作打包成一个原子执行单元——这保证了在极端并发下不会出现超卖。如果把库存扣减放在 MySQL，行锁竞争会让 QPS 掉到三位数。

### 为什么用 RabbitMQ 而不是 Redis Stream/Kafka？

**面试回答**：
> RabbitMQ 是 AMQP 协议的标准实现，Spring AMQP 提供了开箱即用的 `RabbitTemplate` 和 `@RabbitListener` 注解。相比 Redis Stream，RabbitMQ 的消息持久化（写入磁盘）、死信队列（处理失败的消息不丢失）、TTL 延迟队列等特性更适合生产环境。相比 Kafka，RabbitMQ 部署更简单（不需要 ZooKeeper/KRaft），学习曲线平缓，更适合中小规模项目。而且我后续计划部署到服务器，RabbitMQ 的集群能力和管理后台（Web UI 查看队列积压、消费速率）比 Redis Stream 更成熟。

**RabbitMQ vs Kafka 如何选择？**
> RabbitMQ 适合"任务分发"场景（一个消息由一个消费者处理），Kafka 适合"流式数据"场景（一个消息被多个消费者消费）。秒杀订单处理是典型的一对一任务模型，RabbitMQ 更合适。如果后续要做用户行为日志采集（PV/UV 统计），Kafka 更合适。两者并不互斥，成熟项目经常同时使用。

### 为什么用 JWT 而不是 Session？

**面试回答**：
> JWT 是无状态的，Token 存在客户端，服务端不去查 Session，天然支持水平扩展——加多少台后端服务器都不用做 Session 同步。而 Session 方式要么用 Sticky Session（会导致负载不均），要么用 Redis 集中存 Session（单点故障风险）。JWT 的缺点是没法主动踢人下线，但秒杀场景不需要这个能力。

### 前端技术选型

**面试回答**：
> Vue 3 的 Composition API（组合式 API）适合管理复杂页面状态。秒杀详情页有倒计时、按钮状态机（等待→可抢→处理中→成功/失败）、验证码、轮询结果四套状态，用 Options API 会分散到 data/methods/watch 各处，Composition API 可以把同一关注点的逻辑封装在一个 `setup()` 函数中，代码更容易维护。Element Plus 提供了现成的 Countdown、Dialog 等组件，节省开发时间。

---

## 架构总览

```
浏览器 (Vue 3)
    │
    ├── 静态文件 (HTML/CSS/JS) ──▶ Nginx (80端口)
    │                                  │
    └── API 请求 (/api/*) ────────────▶ Spring Boot (8080端口)
                                           │
                          ┌────────────────┼────────────────┬────────────────┐
                          │                │                │                │
                     Redis            MySQL          RabbitMQ          本地文件
                   (库存/限流)      (用户/商品/     (异步订单队列)     (Lua脚本)
                                     订单)               
```

### 一个秒杀请求走过的路径（数据流）

```
用户点击"抢购"
    │
    ▼
[1] Nginx → 转发 POST /api/seckill/1/execute 到 Spring Boot
    │
    ▼
[2] RateLimitInterceptor → Redis Lua 令牌桶检查
    │  超过限制 → 返回 429 (Too Many Requests)
    │  未超限制 → 放行
    ▼
[3] SeckillController → 校验验证码 Token
    │  无效 → 返回错误
    │  有效 → 继续
    ▼
[4] SeckillService.executeSeckill()
    │
    ▼
[5] Redis 执行 Lua 脚本 (一次 EVALSHA，全程原子):
    ① GET seckill:stock:1 → 库存 >0 ?
    ② SISMEMBER seckill:user:1 <userId> → 买过了?
    ③ DECR seckill:stock:1 (扣库存)
    ④ SADD seckill:user:1 <userId> (标记已买)
    ⑤ 返回 SUCCESS + 剩余库存
    │
    ▼
[6] Java 层 RabbitTemplate.convertAndSend()
    把订单消息投递到 RabbitMQ 队列
    │
    ▼
[7] 立即返回给前端: { code: 200, message: "抢购请求已提交" }
    │   总耗时: < 10ms (全程 Redis + RabbitMQ 投递，不触碰 MySQL)
    │
    ▼
[8] RabbitMQ @RabbitListener 消费者 (异步):
      对每条消息:
        ① 生成订单号 → INSERT INTO seckill_order
        ② UPDATE seckill_product ... WHERE version=? AND stock>0 (乐观锁)
        ③ SET seckill:order:result:1:42 = "SUCCESS:ORD2024..."
        ④ 手动 ACK 确认消费
    │
    ▼
[9] 前端每 500ms 轮询 GET /api/seckill/1/result/42
      拿到 SUCCESS → 弹窗"恭喜抢到" + 订单号
      拿到 FAIL → 弹窗失败原因
```

### 三层防超卖（面试核心）

```
Layer 1: Redis Lua 原子化 (命中 ~95% 流量)
   └─ 库存检查、扣减、去重标记在一个原子块完成
   └─ 即使 10000 个请求同时到达，只有 stock=100 个能成功

Layer 2: MySQL 唯一索引 uk_user_seckill(user_id, seckill_product_id)
   └─ 如果 Redis 挂了或重复推送，INSERT 直接报 DuplicateKeyException
   └─ 消费者捕获异常，不扣库存，不创建订单

Layer 3: MySQL 乐观锁 (version 字段)
   └─ UPDATE ... WHERE version=3 AND stock>0
   └─ 如果 affected_rows=0 → 别的消费者已经抢走最后一件
   └─ 消费者捕获，同步 Redis 库存为 0
```

---

## 阶段一：项目脚手架

### Step 1: 创建 Spring Boot 项目

**做什么**：搭建可运行的后端空项目，确保能启动。

**怎么写**：

`/home/ydmy/seckill-platform/backend\` 目录下已经有了 `pom.xml` 和 `SeckillApplication.java`，你现在只需要做：

4. 进入 `backend/` 目录，验证项目能编译：
   ```bash
   cd /home/ydmy/seckill-platform/backend
   ./mvnw compile
   ```
   看到 `BUILD SUCCESS` 就对了。

**为什么这么做**：

> **面试话术**：Maven Wrapper 让项目自带构建工具版本，其他开发者 clone 后不需要手动安装 Maven，直接 `./mvnw` 就能构建，保证 CI/CD 环境一致性。Spring Boot 的 `@SpringBootApplication` 注解 = `@Configuration + @EnableAutoConfiguration + @ComponentScan` 三合一，自动配置的原理是条件注解（`@ConditionalOnClass` 等），类路径上有什么依赖就自动加载什么配置。

**如何验证**：运行 `./mvnw compile` 看到 BUILD SUCCESS。

---

### Step 2: 配置数据库连接

**做什么**：让 Spring Boot 连上你的本地 MySQL。

**写什么代码**：

打开 `/home/ydmy/seckill-platform/backend\src\main\resources\application.yml`（已有模板，改一下你的 MySQL 密码）：

```yaml
server:
  port: 8080

spring:
  datasource:
    # 把下面这行的 password 改成你 MySQL root 的密码
    url: jdbc:mysql://localhost:3306/seckill?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4&allowPublicKeyRetrieval=true
    username: root
    password: 此处改成你的密码
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 30      # 秒杀场景需要更多连接
      minimum-idle: 5
      connection-timeout: 3000

  data:
    redis:
      host: localhost
      port: 6379
      password:                  # Redis 默认无密码，留空
      lettuce:
        pool:
          max-active: 50
          max-idle: 10
          min-idle: 5

  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null   # null 值不返回

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true     # user_name → userName
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印 SQL（开发时用）
  global-config:
    db-config:
      id-type: auto

# JWT 配置
jwt:
  secret: seckill-platform-jwt-secret-key-2024-this-is-a-very-long-secret-for-hs256
  expiration: 604800000  # 7天 (毫秒)
```

**为什么要用 HikariCP 连接池？**

> 面试话术：HikariCP 是 Spring Boot 默认连接池，号称"光速连接池"。它用 ConcurrentBag 无锁数据结构管理连接，bytecode 级别的代理比反射更快。秒杀场景下，消费者批量写数据库时连接数会暴涨，`maximum-pool-size=30` 确保有足够连接而不撑爆 MySQL（默认 max_connections 是 151）。

**如何验证**：
```bash
cd /home/ydmy/seckill-platform/backend
./mvnw spring-boot:run
```
看到 `Started SeckillApplication in X seconds` 就成功了。如果报数据库连接错误，检查密码和 MySQL 是否启动。

---

### Step 3: 建表 + 实体类 + Mapper

**做什么**：执行 SQL 建表，然后创建对应的 Java 实体和 Mapper。

**写什么代码**：

#### 3.1 执行建表脚本

```bash
mysql -u root -p < /home/ydmy/seckill-platform/sql\init.sql
```

> 注意：init.sql 会创建 seckill 数据库和 4 张表。如果想保留已有数据，在 MySQL 命令行中手动执行每条 CREATE TABLE。

#### 3.2 创建实体类 User

新建文件：`backend/src/main/java/com/seckill/entity/User.java`

```java
package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

> **关键注解说明**：
> - `@TableName("user")` — 告诉 MyBatis-Plus 这个实体对应 user 表
> - `@TableId(type = IdType.AUTO)` — 主键自增，由 MySQL 生成 ID
> - `@TableField(fill = FieldFill.INSERT)` — 插入时自动填充值（需配合 MetaObjectHandler）
> - `@Data` — Lombok 注解，自动生成 getter/setter/toString/equals/hashCode

#### 3.3 用同样的模式创建剩余 3 个实体类

新建 `backend/src/main/java/com/seckill/entity/Product.java`：

```java
package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal originalPrice;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

新建 `backend/src/main/java/com/seckill/entity/SeckillProduct.java`：

```java
package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")
public class SeckillProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;

    /** 乐观锁版本号 — 三层防超卖的第三层 */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 以下字段不存在于数据库，用于关联查询展示
    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private BigDecimal originalPrice;

    @TableField(exist = false)
    private String imageUrl;
}
```

> **`@Version` 是什么？**
> MyBatis-Plus 的乐观锁注解。更新时自动在 WHERE 条件加上 `version = ?`，update 成功后 `version + 1`。如果另一个线程先更新了，你的 UPDATE 会受影响行数 = 0，你就知道"抢输了"。

新建 `backend/src/main/java/com/seckill/entity/Order.java`：

```java
package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long seckillProductId;
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer status;   // 0=未支付, 1=已支付, 2=已取消, 3=已退款

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;
}
```

**为什么价格要存在订单里而不是 JOIN 查？**

> 面试话术：订单表的 `seckill_price` 是一个**价格快照**。如果只存外键去 JOIN 产品表，当运营改价后，历史订单的价格就变了——这是严重的财务合规问题。电商系统的铁律：订单必须保存交易时刻的所有关键信息（价格、商品名、收货地址等），不能依赖外键。

#### 3.4 创建 Mapper 接口

新建 `backend/src/main/java/com/seckill/mapper/UserMapper.java`：

```java
package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

`BaseMapper<User>` 已经提供了 `selectById`, `insert`, `updateById`, `deleteById`, `selectList` 等 17 个方法，你只需要声明接口，不用写 SQL。

同样方式创建 `ProductMapper.java`, `SeckillProductMapper.java`, `OrderMapper.java`，都在 `com.seckill.mapper` 包下，分别继承 `BaseMapper<Product>`, `BaseMapper<SeckillProduct>`, `BaseMapper<Order>`。

#### 3.5 配置 Mapper 扫描

打开 `backend/src/main/java/com/seckill/SeckillApplication.java`，确认有这行：

```java
package com.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.seckill.mapper")   // ← 这行必须有
@EnableScheduling                    // Step 11 定时任务要用
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}
```

**如何验证**：
```bash
./mvnw spring-boot:run
```
启动不报错，应该看到 MyBatis-Plus 打印了 Entity 的扫描信息。
没有报 `Table 'seckill.user' doesn't exist` 之类错误说明连上了。

---

## 阶段二：用户认证

### Step 4: 用户注册（BCrypt 密码加密）

**做什么**：实现 `POST /api/auth/register`，接收用户名密码，加密后存入数据库。

**写什么代码**：

#### 4.1 统一响应类

新建 `backend/src/main/java/com/seckill/vo/Result.java`：

```java
package com.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

**为什么统一返回 `{code, message, data}` 结构？**

> 面试话术：前端可以写一个 Axios 响应拦截器统一处理：code != 200 就弹错误提示，code == 401 就跳登录页。如果每个接口返回格式不同，前端就得针对每个接口写不同的错误处理逻辑，维护成本翻倍。这是前后端分离的基础约定。

#### 4.2 请求 DTO

新建 `backend/src/main/java/com/seckill/dto/RegisterDTO.java`：

```java
package com.seckill.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String phone;
    private String email;
}
```

#### 4.3 Bean 配置：注入 BCryptPasswordEncoder

新建 `backend/src/main/java/com/seckill/config/BeanConfig.java`：

```java
package com.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### 4.4 Service 接口和实现

新建 `backend/src/main/java/com/seckill/service/UserService.java`：

```java
package com.seckill.service;

import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;

public interface UserService {
    void register(RegisterDTO dto);
    String login(LoginDTO dto);
}
```

新建 `backend/src/main/java/com/seckill/service/impl/UserServiceImpl.java`：

```java
package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.entity.User;
import com.seckill.exception.BusinessException;
import com.seckill.mapper.UserMapper;
import com.seckill.common.JwtUtils;
import com.seckill.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 密码加密
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt!
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO dto) {
        // 先留空，Step 5 再写
        return null;
    }
}
```

#### 4.5 异常处理类

新建 `backend/src/main/java/com/seckill/exception/BusinessException.java`：

```java
package com.seckill.exception;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}
```

新建 `backend/src/main/java/com/seckill/exception/GlobalExceptionHandler.java`：

```java
package com.seckill.exception;

import com.seckill.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("Duplicate key: {}", e.getMessage());
        return Result.fail(409, "重复操作");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.fail(500, "服务器内部错误");
    }
}
```

#### 4.6 控制器

新建 `backend/src/main/java/com/seckill/controller/AuthController.java`：

```java
package com.seckill.controller;

import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.service.UserService;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        // Step 5 实现
        return null;
    }
}
```

**为什么用 BCrypt 而不是 MD5/SHA-256？**

> 面试话术：MD5 和 SHA-256 是快速哈希，GPU 每秒能算几十亿次，彩虹表可以直接查。BCrypt 有两个优势：一是自带盐值（salt），每个密码的盐不同；二是计算成本可配置（cost factor），默认 10 意味着迭代 2^10=1024 次，暴力破解成本指数级上升。BCrypt 还有一个特性——每次 encode 同一个密码结果都不同（因为随机盐），所以没法用彩虹表。

**如何验证**：启动后端，用 Postman 或 curl 测试：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","phone":"13800138000"}'
```
返回 `{"code":200,"message":"success","data":null}` 就对了。去 MySQL 查 `SELECT * FROM user`，看 password 是否是 `$2a$10$...` 开头。

---

### Step 5: 登录 + JWT + 拦截器

**做什么**：实现登录接口，验证密码后返回 JWT Token。后续所有需要登录的接口通过请求头 Authorization 携带 Token。

**写什么代码**：

#### 5.1 JWT 工具类

新建 `backend/src/main/java/com/seckill/common/JwtUtils.java`：

```java
package com.seckill.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long expiration;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /** 生成 Token */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /** 从 Token 中提取 user_id */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

**解释**：JJWT 0.12.x 版本的 API 和旧版本不同。`subject` 是 JWT 标准字段，我们用它存 userId。`signWith` 使用 HMAC-SHA256 签名，秘钥来自配置文件 `jwt.secret`，不硬编码。

#### 5.2 补充 UserServiceImpl 的 login 方法

打开 `UserServiceImpl.java`，把 login 方法改为：

```java
@Override
public String login(LoginDTO dto) {
    // 1. 查用户
    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>()
            .eq(User::getUsername, dto.getUsername())
    );
    if (user == null) {
        throw new BusinessException("用户名或密码错误");
    }

    // 2. 验密码
    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        throw new BusinessException("用户名或密码错误");
    }

    // 3. 生成 JWT
    return jwtUtils.generateToken(user.getId());
}
```

**为什么错误提示不说"用户名不存在"而说"用户名或密码错误"？**

> 面试话术：这是安全考虑。如果区分提示，攻击者可以枚举哪些用户名已注册（注册了返回"密码错误"，没注册返回"用户不存在"），进而做撞库攻击。模糊提示让攻击者无法判断。

#### 5.3 补充 AuthController 的 login 方法

```java
@PostMapping("/login")
public Result<?> login(@RequestBody LoginDTO dto) {
    String token = userService.login(dto);
    return Result.ok(token);
}
```

#### 5.4 JWT 拦截器

新建 `backend/src/main/java/com/seckill/interceptor/JwtInterceptor.java`：

```java
package com.seckill.interceptor;

import com.seckill.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 如果是 OPTIONS 预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Authorization 头提取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }

        String token = authHeader.substring(7); // 去掉 "Bearer "
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期, 请重新登录\"}");
            return false;
        }

        // 把 userId 存到 request attribute，Controller 里用
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        return true;
    }
}
```

#### 5.5 配置拦截器

新建 `backend/src/main/java/com/seckill/config/WebConfig.java`：

```java
package com.seckill.config;

import com.seckill.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")          // 拦截所有 /api/ 请求
                .excludePathPatterns(
                        "/api/auth/login",           // 登录不拦截
                        "/api/auth/register",        // 注册不拦截
                        "/api/products",             // 商品列表可公开访问 (可选)
                        "/api/products/*/seckill"    // 秒杀详情需要登录但我们先放行看产品
                );
    }
}
```

**如何验证**：
1. 启动项目
2. Postman POST `http://localhost:8080/api/auth/login` body: `{"username":"test","password":"123456"}`
3. 返回的 data 就是一个 JWT 字符串，复制它
4. GET `http://localhost:8080/api/orders`（目前还没实现，但应该返回 500 而不是 401）
5. 在请求头加上 `Authorization: Bearer <刚才复制的Token>`，再发一次
6. 如果返回 500 而不是 401，说明拦截器通过了（500 是因为还没写业务代码）

---

## 阶段三：商品管理

### Step 6: 商品 CRUD（管理员）

**做什么**：管理员可以增删改查商品。先做最简单的分页查询。

**写什么代码**：

#### 6.1 创建 ProductService

新建 `backend/src/main/java/com/seckill/service/ProductService.java`：

```java
package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;

public interface ProductService {
    Page<Product> listProducts(int page, int size);
    Product createProduct(Product product);
    void updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
```

新建 `backend/src/main/java/com/seckill/service/impl/ProductServiceImpl.java`：

```java
package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;
import com.seckill.exception.BusinessException;
import com.seckill.mapper.ProductMapper;
import com.seckill.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> listProducts(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)     // 只查上架商品
               .orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    @Override
    public void updateProduct(Long id, Product product) {
        product.setId(id);
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
```

#### 6.2 创建控制器

新建 `backend/src/main/java/com/seckill/controller/ProductController.java`：

```java
package com.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(productService.listProducts(page, size));
    }
}
```

新建 `backend/src/main/java/com/seckill/controller/AdminProductController.java`：

```java
package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.service.ProductService;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        return Result.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Product product) {
        productService.updateProduct(id, product);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.ok();
    }
}
```

**如何验证**：

```bash
# 创建商品
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <你的Token>" \
  -d '{"name":"iPhone 15","description":"苹果手机","originalPrice":5999,"status":1}'

# 查询商品列表 (不需要 Token，看你怎么配的拦截器)
curl http://localhost:8080/api/products?page=1&size=20
```

测试前先往数据库手动插两条商品：
```sql
INSERT INTO seckill.product (name, description, original_price, status, create_time, update_time)
VALUES ('iPhone 15 Pro', 'A17 Pro 芯片，钛金属设计', 7999.00, 1, NOW(), NOW());

INSERT INTO seckill.product (name, description, original_price, status, create_time, update_time)
VALUES ('MacBook Air M3', '8GB+256GB 星光色', 8999.00, 1, NOW(), NOW());
```

---

### Step 7: 秒杀商品配置

**做什么**：管理员可以配置某个商品在指定时间段内以秒杀价出售。

**写什么代码**：

#### 7.1 创建 SeckillProductService

新建 `backend/src/main/java/com/seckill/service/SeckillProductService.java`：

```java
package com.seckill.service;

import com.seckill.entity.SeckillProduct;

import java.util.List;

public interface SeckillProductService {
    SeckillProduct createSeckillProduct(SeckillProduct sp);
    List<SeckillProduct> listAll();
    SeckillProduct getById(Long id);
}
```

新建 `backend/src/main/java/com/seckill/service/impl/SeckillProductServiceImpl.java`：

```java
package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.entity.Product;
import com.seckill.entity.SeckillProduct;
import com.seckill.exception.BusinessException;
import com.seckill.mapper.ProductMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl implements SeckillProductService {

    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper productMapper;

    @Override
    public SeckillProduct createSeckillProduct(SeckillProduct sp) {
        // 校验商品存在
        Product product = productMapper.selectById(sp.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验秒杀价不能高于原价
        if (sp.getSeckillPrice().compareTo(product.getOriginalPrice()) >= 0) {
            throw new BusinessException("秒杀价必须低于原价");
        }

        // 校验时间
        if (sp.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("秒杀开始时间不能早于当前时间");
        }
        if (sp.getEndTime().isBefore(sp.getStartTime())) {
            throw new BusinessException("秒杀结束时间必须晚于开始时间");
        }

        sp.setStatus(0); // 未开始
        seckillProductMapper.insert(sp);
        return sp;
    }

    @Override
    public List<SeckillProduct> listAll() {
        return seckillProductMapper.selectList(
            new LambdaQueryWrapper<SeckillProduct>()
                .orderByDesc(SeckillProduct::getCreateTime)
        );
    }

    @Override
    public SeckillProduct getById(Long id) {
        return seckillProductMapper.selectById(id);
    }
}
```

#### 7.2 补充 AdminProductController

在 `AdminProductController.java` 中添加：

```java
private final SeckillProductService seckillProductService;  // 加在构造函数参数里

@PostMapping("/seckill-products")
public Result<SeckillProduct> createSeckill(@RequestBody SeckillProduct sp) {
    return Result.ok(seckillProductService.createSeckillProduct(sp));
}

@GetMapping("/seckill-products")
public Result<List<SeckillProduct>> listSeckills() {
    return Result.ok(seckillProductService.listAll());
}
```

注意：`AdminProductController` 的构造函数需要加上 `SeckillProductService` 参数（因为用了 `@RequiredArgsConstructor`）：

```java
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final SeckillProductService seckillProductService;
    // ... 已有方法
}
```

**如何验证**：

```bash
# 给 id=1 的商品设置一个秒杀活动（时间是未来 1 小时）
curl -X POST http://localhost:8080/api/admin/products/seckill-products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <Token>" \
  -d '{
    "productId": 1,
    "seckillPrice": 99.00,
    "stock": 100,
    "startTime": "2026-12-31 10:00:00",
    "endTime": "2026-12-31 12:00:00"
  }'
```

---

后面阶段内容很多，我继续写下去，你一步步跟着做就好。

---

## 阶段四：Redis 集成

### Step 8: 安装 Redis + 配置连接

**做什么**：在 Linux 上安装 Redis，然后配置 Spring Boot 连接。

#### 8.1 安装 Redis (Linux)

```bash
# 安装 Redis
sudo apt install -y redis-server

# 启动服务
sudo systemctl start redis-server

# 设置开机自启
sudo systemctl enable redis-server

# 验证
redis-cli ping
```
返回 `PONG` 即可。

#### 8.2 配置 Spring Boot Redis 连接

打开 `application.yml`，确认 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:     # Redis 默认无密码，留空
      lettuce:
        pool:
          max-active: 50
          max-idle: 10
          min-idle: 5
```

**为什么用 Lettuce 而不是 Jedis？**

> 面试话术：Lettuce 是基于 Netty 的异步非阻塞 Redis 客户端，单连接就能处理多路请求，连接数比 Jedis 少很多。生产环境连接 Redis 通常有连接数限制，Lettuce 的连接复用比 Jedis 更高效。Spring Boot 2.x 开始默认就是 Lettuce。

#### 8.3 创建 RedisConfig

新建 `backend/src/main/java/com/seckill/config/RedisConfig.java`：

```java
package com.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Key 用 String 序列化，避免 \xAC\xED 乱码
        template.setKeySerializer(new StringRedisSerializer());
        // Value 用 JSON 序列化，方便阅读
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
```

**为什么用 StringRedisSerializer 而不是 JdkSerializationRedisSerializer？**

> 面试话术：默认的 JDK 序列化器会产生 `\xAC\xED\x00\x05` 前缀的二进制数据，不可读。StringRedisSerializer 让你在 redis-cli 中 `GET key` 看到明文，排查问题更快。在秒杀路径中，Lua 脚本传入的参数都是字符串，用 StringRedisTemplate 确保类型一致。

**如何验证**：启动项目，没有报 Redis 连接错误即可。后面 Step 9 会写测试验证。（持续更新中...阅读完整步骤）

---

## 以下阶段内容已在代码文件中完整实现，下面列出关键要点

> **说明**：由于 README 篇幅限制，以下阶段的核心代码（Step 9-23）已作为完整源码放在对应包中。这里是**关键要点和面试技巧**的浓缩指南。建议结合源码阅读，自己敲一遍。

---

### Step 9: Redis Key 设计

设计规范化的 Key 前缀，避免散落在各处硬编码。

**面试话术**：把 Redis Key 集中管理有两个好处：一是修改 Key 格式时只改一处；二是 Key 前缀能一眼看出这个数据是干什么用的，Debug 时不用猜。生产环境通常用 `{项目名}:{模块}:{业务含义}:{id}` 的层次结构，清晰的命名是运维的基础。

实际 Key 设计如下：

| Key Pattern | 类型 | 用途 | TTL |
|-------------|------|------|-----|
| `seckill:stock:{spId}` | String | 预热库存数 | 活动结束 |
| `seckill:user:{spId}` | Set | 已购用户集合（防重复） | 活动结束 |
| `seckill:order:result:{spId}:{userId}` | String | 前端轮询结果 | 5分钟 |
| `rate_limit:token:{userId}` | String | 令牌桶当前令牌数 | 60秒 |

> 注意：订单队列改用 RabbitMQ 而不是 Redis Stream，所以不再有 `seckill:order:stream` Key。RabbitMQ 负责消息持久化和可靠投递。

---

### Step 10: Lua 原子扣库存脚本（最关键的一步）

**这是整个项目的核心**。创建 `backend/src/main/resources/lua/seckill_deduct.lua`：

```lua
-- 参数说明:
-- KEYS[1] = seckill:stock:{spId}     -- 库存 key
-- KEYS[2] = seckill:user:{spId}       -- 已购用户集合
-- ARGV[1] = userId

-- 1. 检查库存
local stock = redis.call('GET', KEYS[1])
if not stock or tonumber(stock) <= 0 then
    return {0, 'SOLD_OUT'}
end

-- 2. 检查用户是否已购买
local isMember = redis.call('SISMEMBER', KEYS[2], ARGV[1])
if isMember == 1 then
    return {0, 'REPEAT_PURCHASE'}
end

-- 3. 扣库存
local newStock = redis.call('DECR', KEYS[1])

-- 4. 标记用户已购买
redis.call('SADD', KEYS[2], ARGV[1])

-- 返回: {1, newStock}  -- 成功: code=1 + 剩余库存
return {1, newStock}
```

**面试话术重点**：为什么 Lua 能保证不超卖？为什么不在 Lua 中投递消息？

> Redis 是单线程执行命令的，`EVAL`/`EVALSHA` 执行 Lua 脚本期间，整个脚本作为一个原子操作，不会有其他命令插入进来。这就实现了"读→判断→写"的原子性。10000 个并发用户同时调用这个脚本，Redis 内部排成一个队列逐一执行，库存为 100 时，前 100 个用户返回成功，第 101 个返回 SOLD_OUT。
>
> 注意：Lua 脚本只负责原子地"判断+扣库存+标记用户"，不负责投递消息。消息投递放在 Java 层——SeckillService 收到 Lua 返回的 SUCCESS 后，用 `RabbitTemplate.convertAndSend()` 投递到 RabbitMQ。这样 Lua 脚本更纯粹（只跟 Redis 数据交互），消息队列的选择也更灵活（换 Kafka 只需改 Java 代码，不动 Lua 脚本）。

创建 `backend/src/main/java/com/seckill/config/LuaScriptConfig.java` 把这个 Lua 脚本加载为 Spring Bean：

```java
@Configuration
public class LuaScriptConfig {
    @Bean
    public DefaultRedisScript<List> seckillDeductScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill_deduct.lua"));
        script.setResultType(List.class);
        return script;
    }
}
```

**注意**：Lua 脚本只需要 2 个 KEYS（库存 key + 用户集合 key），不再需要流 key。`LuaScriptConfig` 中的注释也相应更新。

---

### Step 11: 库存预热服务

**面试话术**：预热就是抢购开始前，把 MySQL 中的库存数同步到 Redis。如果不预热，第一个秒杀请求去 MySQL 查库存，后面的请求蜂拥而入时 MySQL 根本扛不住。预热后，整个秒杀流程完全不碰 MySQL（直到异步写单），Redis 单机 10 万 QPS 扛下所有流量。

实现要点：
1. 管理员手动触发 `POST /api/admin/seckill-products/{id}/prewarm`
2. Spring `@Scheduled` 定时扫描"即将开始的秒杀"，自动预热
3. 预热操作 = `SET seckill:stock:{spId} {stock}` + 清理旧的已购集合

---

### Step 12: 秒杀执行逻辑

`SeckillService.executeSeckill(seckillProductId, userId, verificationToken)` 核心步骤：

1. **时间窗口校验** — 当前时间在 startTime ~ endTime 之间
2. **验证码 Token 校验** — 一次性消费 Redis 中的验证 Token
3. **执行 Lua 脚本** — `stringRedisTemplate.execute(seckillDeductScript, keys, args)`
   - keys 只需要 2 个：`stockKey` 和 `userSetKey`（不再有 stream key）
4. **解析返回结果** — code=1 成功 / code=0 SOLD_OUT / code=-1 未开始
5. **如果成功，投递到 RabbitMQ** — `rabbitTemplate.convertAndSend("seckill.order.exchange", "seckill.order", orderMessage)`
6. **立即返回结果给前端** — 不等待订单创建完成

**为什么投递消息放在 Java 层而不是 Lua 里？**

> Lua 脚本应该保持纯粹——只跟 Redis 数据交互。消息队列的选择（RabbitMQ/Kafka）随时可能变化，放在 Java 层可以在不动 Lua 的前提下灵活切换。架构上这叫"缓存层与消息层的解耦"。

**为什么秒杀不直接查数据库？**

> MySQL 的单行更新涉及行锁竞争，在 10 万并发下锁等待会严重拖垮吞吐量，实际 QPS 只能到 1000-2000。Redis 将"查库存+扣库存+去重"三步合并为一次 Lua 原子调用，单机 10 万+ QPS，耗时 < 1ms。秒杀请求不应该等数据库写入完成再返回——用户只需要知道"抢到了"或"抢完了"，订单创建放到后台异步完成。

---

### Step 13: 异步订单消费者（RabbitMQ）

**做什么**：用 Spring AMQP 的 `@RabbitListener` 监听订单队列，异步创建订单。

**写什么代码**：

#### 13.1 RabbitMQ 配置类

新建 `backend/src/main/java/com/seckill/config/RabbitMQConfig.java`：

```java
package com.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机
    public static final String ORDER_EXCHANGE = "seckill.order.exchange";
    // 队列
    public static final String ORDER_QUEUE = "seckill.order.queue";
    // 路由键
    public static final String ORDER_ROUTING_KEY = "seckill.order";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        // durable=true: 队列持久化，RabbitMQ 重启后不丢失
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    // 消息转换器：Java对象 → JSON
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

**为什么用 DirectExchange + 持久化队列？**

> DirectExchange 根据 routing key 精准投递，适合"一个消息由一个消费者处理"的场景。`durable=true` 确保 RabbitMQ 重启后队列和消息不丢失。生产环境建议加上死信队列——处理失败的消息自动转发到死信队列，人工排查后重新投递。

#### 13.2 订单消息 DTO

新建 `backend/src/main/java/com/seckill/dto/OrderMessageDTO.java`：

```java
package com.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessageDTO {
    private Long userId;
    private Long seckillProductId;
    private Long productId;
    private BigDecimal seckillPrice;
}
```

#### 13.3 RabbitMQ 消费者（替代原来的 Redis Stream Consumer）

新建 `backend/src/main/java/com/seckill/consumer/SeckillOrderConsumer.java`：

```java
package com.seckill.consumer;

import com.rabbitmq.client.Channel;
import com.seckill.dto.OrderMessageDTO;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.common.RedisKeyPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderConsumer {

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = "seckill.order.queue", ackMode = "MANUAL")
    public void handleOrderMessage(OrderMessageDTO msg, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            processOrder(msg);
            // 手动确认消费成功
            channel.basicAck(tag, false);
        } catch (DuplicateKeyException e) {
            // 重复下单（用户已买过），确认消费但不扣库存
            log.warn("重复订单被 DB 唯一索引拦截: userId={}, spId={}", msg.getUserId(), msg.getSeckillProductId());
            safeAck(channel, tag);
            // 写失败结果供前端轮询
            writeResult(msg, "FAIL:REPEAT");
        } catch (Exception e) {
            log.error("订单处理失败: {}", msg, e);
            // 拒绝并重新入队（或转入死信队列）
            safeNack(channel, tag);
            writeResult(msg, "FAIL:ERROR");
        }
    }

    private void processOrder(OrderMessageDTO msg) {
        // 1. 生成订单号
        String orderNo = generateOrderNo();

        // 2. 插入订单（DB 唯一索引防重复）
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(msg.getUserId());
        order.setSeckillProductId(msg.getSeckillProductId());
        order.setProductId(msg.getProductId());
        order.setSeckillPrice(msg.getSeckillPrice());
        order.setStatus(0); // 未支付
        orderMapper.insert(order);

        // 3. 扣减 DB 库存（乐观锁）
        SeckillProduct sp = seckillProductMapper.selectById(msg.getSeckillProductId());
        int affected = seckillProductMapper.update(
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SeckillProduct>()
                .setSql("stock = stock - 1, version = version + 1")
                .eq("id", msg.getSeckillProductId())
                .eq("version", sp.getVersion())
                .gt("stock", 0)
        );

        if (affected == 0) {
            // 乐观锁失败，说明库存已被抢完
            orderMapper.deleteById(order.getId());
            stringRedisTemplate.opsForValue().set(
                RedisKeyPrefix.stockKey(msg.getSeckillProductId()), "0");
            writeResult(msg, "FAIL:SOLD_OUT");
            return;
        }

        // 4. 写入成功结果供前端轮询
        writeResult(msg, "SUCCESS:" + orderNo);
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 900000 + 100000);
        return "ORD" + date + random;
    }

    private void writeResult(OrderMessageDTO msg, String result) {
        String key = RedisKeyPrefix.orderResultKey(msg.getSeckillProductId(), msg.getUserId());
        stringRedisTemplate.opsForValue().set(key, result, 300, TimeUnit.SECONDS);
    }

    private void safeAck(Channel channel, long tag) {
        try { channel.basicAck(tag, false); } catch (IOException ignored) {}
    }

    private void safeNack(Channel channel, long tag) {
        try { channel.basicNack(tag, false, true); } catch (IOException ignored) {}
    }
}
```

**面试话术**：

> `@RabbitListener` 注解让 Spring 自动管理消费者线程，`ackMode = "MANUAL"` 确保消息处理成功后才确认——如果在 INSERT 和 UPDATE 之间崩溃，消息回到队列重新消费，配合 DB 唯一索引保证幂等。`prefetch=10` 限制每个消费者线程同时处理的未确认消息数，防止某个消费者负载过高。相比 Redis Stream 的 while(true) 轮询，RabbitMQ 是 push 模型——消息到达主动推送给消费者，延迟更低，CPU 空转更少。

#### 13.4 SeckillService 发送消息

在 `SeckillServiceImpl.executeSeckill()` 中，Lua 返回成功后：

```java
if (resultCode == 1) {
    // Lua 扣库存成功 → 投递订单消息到 RabbitMQ
    OrderMessageDTO msg = new OrderMessageDTO(
        userId, spId, sp.getProductId(), sp.getSeckillPrice()
    );
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.ORDER_EXCHANGE,
        RabbitMQConfig.ORDER_ROUTING_KEY,
        msg
    );
    return SeckillResult.SUCCESS;
}
```

**面试话术**：`convertAndSend` 是异步的——不阻塞 Controller 线程，立即返回。RabbitMQ 的 `publisher confirm` 机制可选开启，确认消息成功写入 Broker 磁盘后再删除 Redis 中的购买标记，保证不丢单。

---

## 阶段五（续）：防护机制

### Step 14: 令牌桶限流

创建 `backend/src/main/resources/lua/rate_limit.lua`：

```lua
-- 令牌桶算法: 限制每个用户每秒最多 N 次请求
-- KEYS[1] = rate_limit:token:{userId}
-- KEYS[2] = rate_limit:last_refill:{userId}
-- ARGV[1] = capacity (桶容量，比如 5)
-- ARGV[2] = refillRate (每秒填充几个令牌，比如 1)
-- ARGV[3] = currentTimeMillis

local tokens = redis.call('GET', KEYS[1])
local lastRefill = redis.call('GET', KEYS[2])

if not tokens then
    -- 首次请求：初始化满桶
    redis.call('SET', KEYS[1], ARGV[1])
    redis.call('SET', KEYS[2], ARGV[3])
    redis.call('EXPIRE', KEYS[1], 60)
    redis.call('EXPIRE', KEYS[2], 60)
    return {1, ARGV[1]}  -- 放行
end

-- 计算应该补充的令牌数
local elapsed = (tonumber(ARGV[3]) - tonumber(lastRefill)) / 1000.0
local tokensToAdd = math.floor(elapsed * tonumber(ARGV[2]))
local currentTokens = math.min(tonumber(tokens) + tokensToAdd, tonumber(ARGV[1]))

if currentTokens > 0 then
    currentTokens = currentTokens - 1
    redis.call('SET', KEYS[1], currentTokens)
    redis.call('SET', KEYS[2], ARGV[3])
    return {1, currentTokens}  -- 放行
else
    return {0, 0}  -- 限流拒绝
end
```

**面试话术：为什么用令牌桶而不是固定窗口？**

> 固定窗口（如"每秒最多 5 次"）在窗口边界有突发问题——用户可以在第一秒的最后 0.1 秒发 5 次，第二秒的最初 0.1 秒又发 5 次，0.2 秒内实际发了 10 次。令牌桶用"匀速填充 + 容量上限"平滑了请求速率。Redis Lua 实现令牌桶还能跨进程共享——多台后端服务器共用同一个限流计数器。

创建 `RateLimitInterceptor.java`，拦截 `/api/seckill/*/execute` 路径，在 JwtInterceptor 之后执行。

---

### Step 15: 乐观锁 + 唯一索引兜底

**代码层面**：
1. `SeckillProduct` 实体已有 `@Version private Integer version;`
2. `MyBatisPlusConfig` 中注册 `OptimisticLockerInnerInterceptor`
3. 消费者中 UPDATE 时用 UpdateWrapper 带 version 条件
4. `seckill_order` 表的 `uk_user_seckill` 唯一索引在 init.sql 中已创建

**面试话术**：三层防超卖各司其职——Redis Lua 是第一道防线（最快，拦截 99% 无效请求），MySQL 唯一索引是第二道防线（兜底，杜绝重复下单），乐观锁是第三道防线（防止库存扣到负数）。每层独立工作，任意一层失效都不影响数据最终一致性。

---

## 阶段六：前端开发

### Step 16: 脚手架 Vue3 项目

```bash
cd E:\project\seckill-platform
npm create vite@latest frontend -- --template vue
cd frontend
npm install
npm install element-plus @element-plus/icons-vue
npm install axios pinia vue-router@4
```

配置 `frontend/vite.config.js` 的代理：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**面试话术**：Vite 开发代理解决了前后端分离项目最常见的跨域问题——开发时前端跑在 localhost:3000，后端在 8080，浏览器的同源策略会拦截请求。`proxy` 配置让 Vite Dev Server 充当反向代理，`/api/*` 的请求转发到后端，Same-Origin 的假象骗过浏览器。

---

### Step 17: 登录注册页 + Axios JWT 拦截器

**核心文件**：

1. `frontend/src/api/request.js` — Axios 实例 + 请求拦截器（自动带 Authorization）+ 响应拦截器（401 跳转登录）
2. `frontend/src/api/auth.js` — `login(data)` 和 `register(data)`
3. `frontend/src/views/Login.vue` — Element Plus 表单
4. `frontend/src/views/Register.vue` — Element Plus 表单
5. `frontend/src/router/index.js` — 路由 + `beforeEach` 导航守卫（未登录跳转）
6. `frontend/src/store/user.js` — Pinia 存 token 和用户信息

**面试话术**：Axios 拦截器是前端处理认证的最佳实践——请求拦截器统一加 Authorization 头，避免每个接口重复写；响应拦截器统一处理 401，省去每个页面判断登录状态的代码。Pinia 存 token 在内存中，localStorage 持久化防止刷新丢失。

---

### Step 18: 商品列表页

`ProductList.vue` — 分页卡片网格，每个卡片显示商品图、名称、原价（有秒杀时划线）、秒杀价（红色大字）、状态徽标（"即将开始"/"抢购中"/"已结束"）。

**面试话术**：秒杀状态的计算——商品列表接口返回每个商品关联的 seckill_product 信息，前端根据 `startTime` 和 `endTime` 与服务器时间对比，判定当前处于哪个阶段。注意：时间对比必须用**服务器时间**，不能用客户端时间（用户可以改本地时钟）。所以后端接口要返回 `serverTime`。

---

### Step 19: 秒杀详情页（核心前端页）

**面试话术**：秒杀页是最复杂的前端页面，涉及四个状态机：

1. **倒计时** — 服务器同步 + 前端每秒递减，每 30 秒重新同步一次纠正漂移
2. **按钮状态** — PENDING(灰) → READY(红) → CLICKED(加载) → SUCCESS(绿)/SOLD_OUT(灰)/REPEAT(灰)
3. **滑块验证码** — 后端生成 targetX，前端拖动对比，后端返回一次性验证 Token
4. **结果轮询** — 秒杀提交后每 500ms 请求一次结果接口，最多轮询 30 秒

Vue 3 Composition API 把四套状态封装在各自的 composable 函数中（`useCountdown`, `useSeckillButton`, `useCaptcha`, `usePolling`），而不是散落在 data/methods 各处。

**关键组件**：
- `CountdownTimer.vue` — 倒计时组件
- `SeckillButton.vue` — 按钮状态机
- `SliderCaptcha.vue` — 滑块验证码
- `OrderResultDialog.vue` — 结果弹窗（轮询）

---

### Step 20: 订单列表页 + 结果轮询

`OrderList.vue` — 展示当前用户的订单，用 Element Plus Table 组件。状态列根据 `status` 字段显示不同颜色标签（未支付/已支付/已取消/已退款）。

---

## 阶段七：集成与测试

### Step 21: Nginx 动静分离

```nginx
server {
    listen 80;
    server_name localhost;

    # 静态文件（Vue build 产物）
    location / {
        root /home/ydmy/seckill-platform/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;  # SPA 路由回退
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**面试话术**：动静分离的意义——前端 HTML/JS/CSS/图片都是静态资源，让 Nginx 直接返回，不经过 Spring Boot 的 Tomcat，减少后端压力。Nginx 单机能轻松处理几万静态请求/秒，反向代理到后端只处理动态 API。`try_files $uri /index.html` 解决 Vue Router history 模式下刷新 404 的问题。

---

### Step 22: JMeter 压测验证

验证清单（面试时可以报出这些数字）：

- [ ] 创建 100 库存的秒杀
- [ ] 预热到 Redis
- [ ] 预热 1000 个 JWT Token（给每个虚拟用户分配一个）
- [ ] JMeter: 1000 线程，100 并发，1 秒 ramp-up
- [ ] 验证结果：
  - 100 个订单生成（不多不少）
  - 900 个请求返回"已售罄"
  - 0 个超卖
  - 0 个重复下单
  - P99 延迟 < 50ms

**面试话术**：压测不要只在成功路径上跑——模拟各种边界场景：秒杀开始前点击、结束后点击、同一用户连续点击、Token 过期、验证码错误。一个合格的高并发系统，失败路径的处理比成功路径更重要。

---

### Step 23: 异常处理完善

补充 `GlobalExceptionHandler` 处理这些情况：
- `MethodArgumentNotValidException` → 参数校验失败（需要先加 `spring-boot-starter-validation` 依赖）
- `MissingRequestHeaderException` → 缺少 Authorization 头
- `HttpMessageNotReadableException` → JSON 格式错误

---

## 面试问答 20 题

> 面试前花 30 分钟过一遍这些问题，能回答出来就差不多了。

### 基础篇

**Q1: 这个项目的核心难点是什么？**
> 高并发下的库存一致性。传统做法是在数据库用 `SELECT ... FOR UPDATE` 行锁，但行锁会让 QPS 掉到 1000。我用"Redis Lua 原子扣减 + DB 乐观锁 + DB 唯一索引"三层防超卖机制，秒杀响应在 Redis 层就返回，用户不需要等 DB 写入。

**Q2: 为什么不能只靠 Redis 扣库存？**
> Redis 是内存数据库，虽然有 RDB/AOF 持久化，但极端情况下（断电）可能丢失数据。MySQL 的 ACID 事务是最可靠的数据保障。所以 Redis 承担"拦截流量"角色，MySQL 是最终数据的"source of truth"。两层结合：Redis 越快越好，MySQL 越稳越好。

**Q3: 秒杀 QPS 能到多少？瓶颈在哪？**
> 在我的配置下，Redis 层秒杀响应 < 5ms，单机 Spring Boot 的 Tomcat 默认 200 线程，实际约 5000 QPS。瓶颈是：
> 1. Tomcat 线程数（调大 max-threads 可提升）
> 2. Spring Boot Controller 层创建了大量临时对象（GC 压力）
> 3. 网络带宽
> 
> 如果要突破，方案是：Nginx + OpenResty 做一层 Lua 限流，Redis Cluster 分片，后端多实例水平扩展。

**Q4: Lua 脚本怎么防重放攻击？**
> Token 是一次性的，验证后立即 DELETE。滑块验证码也绑定了 seckillProductId 和 UUID，验证后删除。更严格的话可以加入请求签名——前端对 (timestamp + nonce + body) 做 HMAC，后端验证时间戳不超过 5 秒。

**Q5: 如果 Redis 挂了呢？**
> 首先是运维层面——Redis Sentinel 主从自动切换，保证高可用。代码层面：捕获 Redis 连接异常，降级为直接查数据库（虽然慢，但不会完全不可用）。同时响应给前端"系统繁忙，请稍后重试"。

**Q6: 为什么用 StringRedisTemplate 而不是 RedisTemplate<Long, Object>？**
> 秒杀路径只传简单的字符串和数字。`StringRedisTemplate` 省去了序列化/反序列化开销，Lua 脚本的 KEYS 和 ARGV 都是 String 类型，用 String 类型避免类型转换异常。`GenericJackson2JsonRedisSerializer` 会把类全限定名也写进 JSON，浪费空间且反序列化慢。

### 进阶篇

**Q7: 消息队列如果丢消息怎么办？**
> RabbitMQ 配合手动 ACK（`ackMode = "MANUAL"`）实现 at-least-once 语义——消费者处理成功才 `basicAck`，处理失败 `basicNack` 让消息重新入队。消息和队列都声明了 `durable=true`，RabbitMQ 重启后不丢失。代码层面需要幂等——消费端通过 DB 唯一索引防重，重复消费同一消息不会创建重复订单。极端情况下可以配置死信队列（DLX），处理失败超过 N 次的消息转入死信队列由人工排查。

**Q8: 如何进行数据库层面的优化？**
> 1. 索引优化：`seckill_order` 上 `uk_user_seckill` 覆盖"查某人是否已买"和"防重复 INSERT"两个需求
> 2. SQL 优化：避免在循环中进行 `selectById`，批量操作用 `selectBatchIds`
> 3. 连接池调优：HikariCP maximumPoolSize=30，应对消费者突发写入
> 4. 读写分离：商品查询走从库，订单写入走主库

**Q9: 为什么要分三层防超卖？只做一层不行吗？**
> 每层职责不同，互相兜底：
> - Layer 1 (Redis Lua): 极速拦截，99% 的请求在这里被处理（无论成功还是失败）
> - Layer 2 (DB 唯一索引): 防止同一个用户重复购买，这是最可靠的保证（数据库约束无法绕过）
> - Layer 3 (DB 乐观锁): 防止库存扣到负数，比如 Redis 和 MySQL 库存数不一致时的最后防线
> 
> 这个设计遵循 Defense in Depth（纵深防御）原则——在分布式系统中，任何单一防护都可能失效，多层才能保证最终一致性。

**Q10: 乐观锁和悲观锁怎么选？**
> 秒杀场景用乐观锁。悲观锁（`SELECT ... FOR UPDATE`）在秒杀场景会让所有请求排队等行锁，吞吐量极低。乐观锁（version 字段）假设冲突不频繁，只在 UPDATE 时检查——但实际上秒杀冲突非常频繁！所以乐观锁放在最后（异步消费者中），前面的 Redis Lua 已经过滤了绝大部分并发冲突，到达乐观锁时冲突概率已大大降低。

**Q11: 如果秒杀活动已经结束，但消费者还在处理积压的消息呢？**
> 消费者在 `processOrder` 中追加时间校验——判断 `LocalDateTime.now()` 是否在 `seckill_product.end_time` 之前。如果已结束，不再创建订单，直接 `basicAck` 确认消费（跳过），并写 result cache "FAIL:ENDED"。RabbitMQ 也可以给队列设置 TTL——超过活动结束时间的消息自动过期丢弃。

**Q12: 为什么不直接用 `DECR` 而要写 Lua 脚本？**
> `DECR` 只能扣库存，不能同时检查用户是否重复购买。如果把"检查重复"和"扣库存"分成两次 Redis 命令，中间有时间窗口——两个请求可能同时通过重复检查，然后都成功扣库存。Lua 把多步操作打包成一个原子事务，消除了这个竞态窗口。

**Q13: JWT 怎么主动踢人下线？**
> JWT 本身没有这个能力（无状态特性导致）。解决方案：Redis 维护一个 Token 黑名单，`logout` 时把 Token 的剩余有效期加入黑名单。JwtInterceptor 除了验证签名，还要查黑名单。不过秒杀场景通常不需要踢人功能——可以放到面试扩展话题说。

### 系统设计篇

**Q14: 如果流量再大 10 倍（百万 QPS），怎么扩展？**
> 1. **前端层**: CDN 缓存静态资源，减少回源带宽
> 2. **网关层**: Nginx/OpenResty WAF 层做恶意请求过滤 + IP 级别限流
> 3. **应用层**: Spring Boot 多实例 + Nginx upstream 负载均衡（轮询/最少连接）
> 4. **缓存层**: Redis Cluster 按 seckillProductId 分片，不同秒杀活动落在不同 Redis 节点
> 5. **数据库层**: 
>    - 读写分离：主库写入，从库查询
>    - 分库分表：按 user_id 水平拆分订单表（ShardingSphere）
>    - 上 TiDB 或 Oceanbase 等分布式数据库
> 6. **消息队列**: 换用 Kafka（分区并行消费），或增加 RabbitMQ 集群提高吞吐

**Q15: 秒杀 URL 怎么防提前暴露？**
> 1. 秒杀开始前，后端只返回倒计时，不返回 seckillProductId
> 2. 活动开始时，后端生成一个动态 Token 作为 URL 参数
> 3. Token 有时效性（5分钟），过期失效
> 4. URL 本身可以是 MD5(seckillProductId + timestamp + salt)，不可预测

**Q16: 为什么要做动静分离？前端不能直接放 Spring Boot 的 static 吗？**
> Spring Boot 的 Tomcat 处理静态文件效率远不如 Nginx。Nginx 采用 sendfile 系统调用，零拷贝传输文件，而 Tomcat 要走 Java NIO 再拷贝一次。更重要的是，动静分离后可以对静态资源单独优化——CDN 推送、长时间缓存、Gzip 压缩。

**Q17: 滑块验证码的后端是怎么实现的？**
> 1. 前端请求 `GET /api/seckill/generate-verification/{spId}` 
> 2. 后端生成 UUID，随机一个 targetX（0-300px 之间）
> 3. `SET seckill:verification:{spId}:{uuid} targetX EX 300`（5分钟过期）
> 4. 前端拖动滑块，用户松手后把 (uuid, sliderX) 发给 `POST /api/seckill/verify`
> 5. 后端 GET Redis 中的 targetX，比较 `|sliderX - targetX| < 5px` 则通过
> 6. DELETE 这个 Key（一次性消费），返回一个新的 verificationToken
> 7. 真正秒杀时带着这个 verificationToken

**Q18: 你的项目里前端轮询为什么是 500ms 而不是 WebSocket？**
> 轮询简单可靠，不需要维持长连接。500ms 间隔对用户体验没影响——秒杀提交到订单创建通常 1-3 秒，前端只需要更新一次状态。WebSocket 在这个场景是过度设计，增加了连接管理复杂度（重连、心跳），而收益不大。

**Q19: 你怎么保证订单号全局唯一？**
> 订单号生成器使用雪花算法（Snowflake）的思路：timestamp(41bit) + datacenter(5bit) + worker(5bit) + sequence(12bit)。但因为是单机项目，我简化成 `ORD + yyyyMMddHHmmss + 6位随机数`，在加上 DB 的 UNIQUE KEY 约束，插入失败就重新生成。

**Q20: 如果让你重新设计这个系统，你会改什么？**
> 1. **把验证码独立成微服务**：滑块验证是 CPU 密集型（图片处理），和瓶颈路径隔离
> 2. **消息队列升级**：当前用 RabbitMQ，如果流量继续增长可升级到 Kafka——Kafka 的分区并行消费和持久化机制在超大流量下更成熟
> 3. **订单服务独立**：秒杀服务和订单服务拆分，秒杀只负责扣库存发消息，订单服务独立部署
> 4. **加可观测性**：Micrometer + Prometheus + Grafana 做监控面板，OpenTelemetry 做链路追踪
> 5. **前端骨架屏 + Service Worker**：秒杀页做到秒开，提升用户体验

---

## 项目效果总结（写在简历上）

> **可量化的成果**：
> - 实现三层防超卖机制，JMeter 压测 1000 并发/100 库存 → 零超卖、零重复
> - 秒杀接口响应时间 < 10ms（全程 Redis 操作，不访问数据库）
> - 令牌桶限流 + 滑块验证码，有效防御黄牛脚本攻击
> - RabbitMQ 异步削峰，订单创建平滑稳定，支持手动 ACK + 消息持久化
> - 前后端分离架构，Vue3 + Spring Boot，Nginx 动静分离

---

## 最后提醒

1. **不要一次性看懂所有代码**——跟着步骤逐阶段实现，每完成一个 Step 就运行验证
2. **把报错信息当成最好的老师**——每个报错背后都是一个知识点
3. **敲代码时多想"为什么这么写"**——这是面试时真正会被问的
4. **项目完成后，把面试 20 题过一遍**——能回答上来才算真正理解了秒杀系统

祝学有所成，面试顺利！
