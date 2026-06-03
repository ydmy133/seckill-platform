# 秒杀平台 (Seckill Platform) — 零基础全栈教程

> 这本教程假设你 **没学过 Java、没接触过 Spring Boot**。我会把每个概念掰开了讲，让你不仅"抄对了代码"，还知道**每一行在干什么**。

---

## 使用说明

**怎么读这本教程？**

1. **按顺序读**，不要跳。每个新概念第一次出现时会详细解释，后面再遇到就简略带过。
2. **每个模块 = 后端代码 + 前端代码 + 对接验证**。写完就能在浏览器里看到效果。
3. **代码里的注释就是教材**。打开 `.java` 和 `.vue` 文件，每一行都有中文解释。
4. **先理解"为什么"，再动手写**。每个章节开头会用一个生活例子解释要做什么。

**阅读约定：**

| 符号 | 含义 |
|------|------|
| `代码块` | 你需要创建/修改的完整文件内容 |
| > 引用块 | 面试话术、扩展知识（看不懂可以先跳过） |
| **加粗** | 重要概念，需要记住 |

---

## 目录

- [第零章：在写代码之前 —— Java 世界速览](#第零章在写代码之前--java-世界速览)
- [第一章：模块 0 —— 理解已有代码](#第一章模块-0--理解已有代码)
- [第二章：模块 1 —— 用户注册（前后端联动）](#第二章模块-1--用户注册前后端联动)
- [第三章：模块 2 —— 用户登录 + JWT（前后端联动）](#第三章模块-2--用户登录--jwt前后端联动)
- [第四章：模块 3 —— 商品管理（前后端联动）](#第四章模块-3--商品管理前后端联动)
- [第五章：模块 4 —— 秒杀核心（前后端联动）](#第五章模块-4--秒杀核心前后端联动)
- [第六章：模块 5 —— 限流与防护](#第六章模块-5--限流与防护)
- [第七章：模块 6 —— 订单与收尾](#第七章模块-6--订单与收尾)
- [第八章：学习路线总结](#第八章学习路线总结)
- [附录：面试 20 问](#附录面试-20-问)

---

## 第零章：在写代码之前 —— Java 世界速览

### 0.1 Java 是什么？

Java 是一门**编译型**语言，和 JavaScript 没有任何关系（名字像纯属营销）。

```
你写的代码         编译后的文件        谁在运行
─────────        ──────────        ──────────
.js  (JavaScript) → 不需要编译  → 浏览器 / Node.js 直接运行
.java (Java)      → .class 字节码 → JVM（Java 虚拟机）运行
```

**类比**：如果你用过 TypeScript，`tsc` 把 `.ts` 编译成 `.js`。Java 的 `javac` 也是同样的道理 —— 把 `.java` 编译成 `.class`，然后 JVM 执行 `.class`。

项目里你不需要手动敲 `javac`，Maven 会帮你搞定。

---

### 0.2 Maven 是什么？

**一句话：Maven = Java 世界的 npm。**

| npm 概念 | Maven 对应 | 说明 |
|----------|-----------|------|
| `package.json` | `pom.xml` | 声明项目用了哪些第三方库 |
| `npm install` | `mvn install` / Maven Wrapper 自动下载 | 下载依赖 |
| `node_modules/` | `~/.m2/repository/` | 依赖存放位置 |
| `npm run dev` | `./mvnw spring-boot:run` | 启动项目 |

项目根目录有个 `mvnw` 文件（Maven Wrapper），它就像 `npx` —— 不需要你提前装好 Maven，运行 `./mvnw` 时会自动下载正确版本。

---

### 0.3 Spring Boot 是什么？

**一句话：Spring Boot = Java 世界里搭好的项目脚手架。**

类比：你用 `npm create vite@latest` 创建 Vue 项目，`vite` 帮你生成了目录结构、配置好了打包工具。

Spring Boot 做的事一样：
- 内嵌了一个 Tomcat 服务器（不用单独装 Nginx/Apache）
- 自动配置了数据库连接、JSON 转换、日志等
- 你只需要写业务代码

---

### 0.4 一个 HTTP 请求走过的路

理解这个，就理解了整个项目的架构。以后台用户注册为例：

```
[前端浏览器]                    [后端 Spring Boot]
    │                                │
    │  1. 用户填写表单，点击"注册"      │
    │                                │
    │  2. POST /api/auth/register    │
    │     Body: {"username":"abc",   │
    │            "password":"123"}   │
    │ ─────────────────────────────▶ │
    │                                │  3. WebConfig（拦截器检查：注册接口不需要登录，放行）
    │                                │
    │                                │  4. AuthController.register()
    │                                │     接收 JSON → 转成 RegisterDTO 对象
    │                                │
    │                                │  5. UserServiceImpl.register()
    │                                │     检查用户名是否已存在
    │                                │     BCrypt 加密密码
    │                                │
    │                                │  6. UserMapper.insert()
    │                                │     INSERT INTO user ...
    │                                │
    │                                │  7. 返回 Result.ok()
    │  ◀─────────────────────────────│      {"code":200,"message":"success"}
    │                                │
    │  8. 前端显示"注册成功"            │
```

**核心规则**：Controller 只负责接收请求和返回结果，**不写业务逻辑**。业务逻辑全在 Service 层。Service 不直接写 SQL，通过 Mapper 操作数据库。

---

### 0.5 项目中你会遇到的 10 种文件

一个 Spring Boot 项目里有很多种 `.java` 文件，每种有不同的职责。这张表先看一遍有个印象，后面每遇到一种都会详细讲：

| 类型 | 包路径 | 一句话解释 | 生活类比 |
|------|--------|-----------|----------|
| **启动类** | 根目录 | 项目的"开关"，双击运行 | 汽车点火按钮 |
| **Entity（实体）** | `entity/` | 一张数据库表 = 一个 Entity 类 | 表格的模板/模具 |
| **Mapper** | `mapper/` | 操作数据库的接口（增删改查） | 电视遥控器 |
| **Service 接口** | `service/` | 定义有哪些业务功能（只有方法名） | 合同/契约 |
| **ServiceImpl** | `service/impl/` | 业务逻辑的具体实现代码 | 合同的执行人 |
| **Controller** | `controller/` | 接收前端请求，返回 JSON | 餐厅前台服务员 |
| **DTO** | `dto/` | 前端发给后端的 JSON 数据结构 | 快递包裹 |
| **VO（Result）** | `vo/` | 后端统一返回格式 | 标准回执单 |
| **Config** | `config/` | 告诉 Spring 怎么创建和管理对象 | 工厂车间配置 |
| **Exception** | `exception/` | 统一处理错误，返回友好提示 | 安全网 |
| **Interceptor** | `interceptor/` | 请求到达 Controller 前做检查 | 门禁安检 |
| **Common/Utils** | `common/` | 可复用的工具代码 | 工具箱 |

---

### 0.6 常用注解速查表

注解（Annotation）是 Java 里以 `@` 开头的东西，用来给代码加"标签"。Spring 看到这些标签就知道该怎么处理。

| 注解 | 放在哪 | 作用（大白话） |
|------|--------|---------------|
| `@SpringBootApplication` | 启动类 | 告诉 Spring："这是入口，请从这里启动整个应用" |
| `@RestController` | Controller 类 | "这个类的所有方法返回值直接变成 JSON" |
| `@RequestMapping("/xxx")` | Controller 类 | "这个 Controller 管 /xxx 开头的所有请求" |
| `@PostMapping("/yyy")` | Controller 方法 | "这个方法处理 POST /xxx/yyy 请求" |
| `@GetMapping("/yyy")` | Controller 方法 | "这个方法处理 GET /xxx/yyy 请求" |
| `@RequestBody` | Controller 方法参数 | "把前端发来的 JSON 字符串自动转成 Java 对象" |
| `@Service` | ServiceImpl 类 | "这是一个业务逻辑类，Spring 请管理它" |
| `@Component` | 工具类/拦截器 | "这是一个通用组件，Spring 请管理它" |
| `@Configuration` | Config 类 | "这是一个配置类，Spring 请先读它" |
| `@Bean` | Config 类的方法 | "这个方法返回的对象，请加到 Spring 的仓库里" |
| `@Mapper` | Mapper 接口 | "这是 MyBatis-Plus 的数据库操作接口" |
| `@Data` | Entity/DTO/VO | Lombok："自动生成 getter/setter/toString" |
| `@RequiredArgsConstructor` | 任何需要注入的类 | Lombok："为 final 字段自动生成构造函数" |
| `@TableName("xxx")` | Entity 类 | "这个类对应的数据库表叫 xxx" |
| `@TableId(type = IdType.AUTO)` | Entity 的主键字段 | "这是主键，由数据库自增生成" |
| `@Version` | Entity 字段 | MyBatis-Plus："更新时自动检查这个字段（乐观锁）" |
| `@Override` | 方法 | "我在重写父类/接口的方法，帮我检查签名对不对" |

---

### 0.7 依赖注入是什么？（先读一遍，后面会遇到实践）

**依赖注入** 是 Spring 最核心的概念，也是 Java 新手最容易懵的地方。

**生活例子**：你去餐厅吃饭，服务员（Controller）不需要自己买菜洗菜（创建 Service 对象），厨房（Spring）会自动把菜备好递给他。

**代码层面**：

```java
// ❌ 没有依赖注入时，你得自己 new：
public class AuthController {
    private UserService userService = new UserServiceImpl(); // 自己创建，紧密耦合
}

// ✅ 有了依赖注入，Spring 帮你搞定：
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;  // 只声明我需要，Spring 会自己找到并注入
}
```

`@RequiredArgsConstructor` 是 Lombok 提供的注解，会自动生成一个把 `final` 字段作为参数的构造函数。Spring 看到这个构造函数，就去自己的"对象仓库"里找到对应的 Bean（对象），自动传进来。

**你只需要记住**：
1. 在类上加 `@RequiredArgsConstructor`
2. 把需要的依赖声明为 `private final` 字段
3. Spring 会自动注入，不需要 `new`

---

## 第一章：模块 0 —— 理解已有代码

> 在你开始写新代码之前，让我们先读一遍现有的代码。这些代码在之前的步骤中已经创建好了。**不要急着写，先读懂每一行。**

### 1.0 当前项目里有什么？

打开 `backend/src/main/java/com/seckill/`，你应该看到这些文件：

```
SeckillApplication.java          ← 项目的启动入口
├── entity/                      ← 实体类（对应数据库表）
│   ├── User.java
│   ├── Product.java
│   ├── SeckillProduct.java
│   └── Order.java
├── mapper/                      ← 操作数据库的接口
│   ├── UserMapper.java
│   ├── ProductMapper.java
│   ├── SeckillProductMapper.java
│   └── OrderMapper.java
├── dto/                         ← 前端发来的数据格式
│   ├── RegisterDTO.java
│   └── LoginDTO.java
├── vo/                          ← 返回给前端的格式
│   └── Result.java
├── service/                     ← 业务逻辑
│   ├── UserService.java         （接口）
│   └── impl/
│       └── UserServiceImpl.java （实现）
├── controller/                  ← 接收 HTTP 请求
│   └── AuthController.java
├── config/                      ← Spring 配置
│   ├── BeanConfig.java
│   └── WebConfig.java
├── exception/                   ← 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── common/                      ← 工具类
│   └── JwtUtils.java
└── interceptor/                 ← 请求拦截器
    └── JwtInterceptor.java
```

**现在不用深究每个文件的内容**，只需要知道它们的大致分类。后面我们会一个一个打开读写。

---

### 1.1 先看数据库里有什么

在写 Java 代码之前，先搞清楚数据存在哪、长什么样。

打开你的 MySQL 客户端，看看 `seckill` 数据库（你用 VSCode 插件或者命令行都行）：

```sql
USE seckill;
SHOW TABLES;
```

应该看到 4 张表：

| 表名 | 存什么 | 一句话 |
|------|--------|--------|
| `user` | 用户信息 | 注册时写入，登录时查询 |
| `product` | 商品基础信息 | 商品名称、原价、描述 |
| `seckill_product` | 秒杀活动配置 | 哪个商品、秒杀价格、库存、开始结束时间 |
| `seckill_order` | 秒杀订单 | 谁抢到了哪个秒杀、成交价格 |

看一下 `user` 表的结构：

```sql
DESC user;
```

| 字段 | 类型 | 含义 |
|------|------|------|
| `id` | BIGINT | 自增主键，每新增一个用户 +1 |
| `username` | VARCHAR(64) | 用户名，唯一（不能重复） |
| `password` | VARCHAR(256) | BCrypt 加密后的密文 |
| `phone` | VARCHAR(20) | 手机号，可空 |
| `email` | VARCHAR(128) | 邮箱，可空 |
| `status` | TINYINT | 1=正常 0=禁用 |
| `create_time` | DATETIME | 注册时间 |
| `update_time` | DATETIME | 最后修改时间 |

---

### 1.2 启动类 —— 项目的点火开关

打开 [SeckillApplication.java](backend/src/main/java/com/seckill/SeckillApplication.java)

```java
package com.seckill;                // 声明包路径，这个文件在 com/seckill/ 目录下

import org.mybatis.spring.annotation.MapperScan;       // 引入 @MapperScan 注解，用于扫描 Mapper 接口
import org.springframework.boot.SpringApplication;      // Spring Boot 启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // 自动配置注解
import org.springframework.scheduling.annotation.EnableScheduling;   // 开启定时任务功能

@SpringBootApplication   // 核心注解：标记这是启动类 + 自动配置 + 自动扫描同包下的所有组件
@MapperScan("com.seckill.mapper")  // 告诉 MyBatis-Plus 去哪找 Mapper 接口
@EnableScheduling                  // 开启定时任务（后面库存预热会用到）
public class SeckillApplication {  // 整个应用从这里开始

    public static void main(String[] args) {                   // Java 程序的入口方法
        SpringApplication.run(SeckillApplication.class, args); // 启动 Spring Boot
    }
}
```

**每行解释**：

- `package com.seckill;` — Java 的文件组织方式，类比文件系统里的文件夹
- `import ...` — 引入别的包里的类，类比 JavaScript 的 `import` 或 `require`
- `@SpringBootApplication` — Spring 最核心的注解，三合一：
  1. `@Configuration` — 允许定义 Bean
  2. `@EnableAutoConfiguration` — 根据 pom.xml 里的依赖自动配置（比如有 MySQL 驱动就自动配数据源）
  3. `@ComponentScan` — 自动扫描当前包及子包下所有带注解的类
- `@MapperScan("com.seckill.mapper")` — MyBatis-Plus 需要知道 Mapper 接口在哪
- `@EnableScheduling` — 允许使用 `@Scheduled` 定时任务
- `public static void main(String[] args)` — Java 程序的固定入口，和 C 语言的 `main()` 一样
- `SpringApplication.run(...)` — 启动整个 Spring 容器

**类比**：这个文件相当于 `package.json` 里的 `"scripts": { "start": "node index.js" }` + `index.js` 的合体。

---

### 1.3 Entity（实体类）—— 数据库表的"镜子"

数据库有的 4 张表，对应 Java 里要有 4 个实体类。

**为什么需要实体类？**

Java 是面向对象的语言，操作"对象"比操作"SQL 结果集"方便。实体类就是让表的每一行数据变成一个 Java 对象：

```
数据库 user 表的一行：                          Java User 对象：
┌────┬──────────┬──────────────────┬─────┐      User {
│ id │ username │ password         │ ... │        id: 1,
│ 1  │ "张三"   │ "$2a$10$abc..."  │ ... │        username: "张三",
└────┴──────────┴──────────────────┴─────┘        password: "$2a$10$abc...",
                                                  ...
                                                }
```

打开已有文件 [entity/User.java](backend/src/main/java/com/seckill/entity/User.java)，逐个注解理解：

```java
@Data                     // Lombok 注解：编译时自动生成 getXxx()、setXxx()、toString()、equals()、hashCode()
@TableName("user")        // 告诉 MyBatis-Plus：这个实体类对应数据库的 user 表
public class User{        // 一个 User 对象 = user 表的一行数据

    @TableId(type = IdType.AUTO)  // 标记这是主键，AUTO = 由 MySQL 自增生成
    private Long id;

    private String username;      // 字段名自动映射到表的 username 列
    private String password;      // 数据库中存的是 BCrypt 密文
    // ... 其他字段
}
```

**关键注解讲解**：

- **`@Data`**：Lombok 提供。编译时自动生成 `getId()`, `setId()`, `getUsername()`, `setUsername()` 等方法。你不用手写，但可以照常调用。
- **`@TableName("user")`**：显式指定对应的数据库表名。不写的话 MyBatis-Plus 默认类名 = 表名（User → user）。
- **`@TableId(type = IdType.AUTO)`**：告诉 MyBatis-Plus，这个主键值由数据库自动生成（AUTO_INCREMENT），插入时不需要手动赋值。
- **`@TableField(fill = FieldFill.INSERT)`**：插入数据时自动填充当前时间。Spring 中有一个 `MetaObjectHandler` 负责这个自动填充逻辑（后续会讲到）。

**同样方式看另外 3 个实体**：[Product.java](backend/src/main/java/com/seckill/entity/Product.java)、[SeckillProduct.java](backend/src/main/java/com/seckill/entity/SeckillProduct.java)、[Order.java](backend/src/main/java/com/seckill/entity/Order.java)。

其中 `SeckillProduct.java` 有一个特殊注解值得注意：

```java
@Version                    // MyBatis-Plus 乐观锁注解
private Integer version;    // 更新时自动 WHERE version=? 然后 SET version=version+1
```

这是三层防超卖的第三层，后面模块 5 详细讲。

---

### 1.4 Mapper —— 操作数据库的"遥控器"

实体类定义了数据结构，但要真正去数据库做增删改查，靠的是 Mapper。

**请打开** [mapper/UserMapper.java](backend/src/main/java/com/seckill/mapper/UserMapper.java)：

```java
@Mapper                        // 告诉 Spring：这是一个 MyBatis 的 Mapper 接口
public interface UserMapper extends BaseMapper<User> {   // 继承 BaseMapper<User> 是关键
    // 空的！但是已经拥有 17 个数据库操作方法
}
```

**为什么这个空接口有 17 个方法？**

`BaseMapper<T>` 是 MyBatis-Plus 提供的基础接口，内置了常用 CRUD 方法。`extends BaseMapper<User>` 意味着 `UserMapper` 直接继承这些方法：

| 方法 | 对应的 SQL | 说明 |
|------|-----------|------|
| `selectById(1L)` | `SELECT * FROM user WHERE id=1` | 按主键查一行 |
| `selectList(wrapper)` | `SELECT * FROM user WHERE ...` | 条件查询，返回列表 |
| `selectCount(wrapper)` | `SELECT COUNT(*) FROM user WHERE ...` | 查符合条件的数量 |
| `insert(user)` | `INSERT INTO user (...) VALUES (...)` | 插入一行 |
| `updateById(user)` | `UPDATE user SET ... WHERE id=?` | 按主键更新 |
| `deleteById(1L)` | `DELETE FROM user WHERE id=1` | 按主键删除 |
| ... | ... | 还有 11 个 |

**关键概念**：

- **`interface`** vs **`class`**：Java 里 `interface`（接口）只声明方法签名，不写实现代码。`class` 是具体实现。Mapper 是接口，MyBatis-Plus 在运行时会自动生成代理类来真正执行 SQL。
- **`extends BaseMapper<User>`**：User 是泛型参数，告诉 BaseMapper 这个 Mapper 操作的是 `user` 表。

**另外 3 个 Mapper** 同理：`ProductMapper extends BaseMapper<Product>`、`SeckillProductMapper extends BaseMapper<SeckillProduct>`、`OrderMapper extends BaseMapper<Order>`。

---

### 1.5 application.yml —— 项目的中枢配置文件

打开 [application.yml](backend/src/main/resources/application.yml)，逐段理解：

```yaml
server:
  port: 8080                          # Spring Boot 启动后监听 8080 端口（浏览器访问 localhost:8080）

spring:
  datasource:                          # 数据库连接配置
    url: jdbc:mysql://localhost:3306/seckill?...  # JDBC 连接字符串：MySQL 在本地 3306 端口，数据库名 seckill
    username: root                     # MySQL 用户名
    password: 123123                   # MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver  # MySQL 驱动类
    hikari:
      maximum-pool-size: 30            # 连接池最多 30 个连接（秒杀场景需要更多）
      minimum-idle: 5                  # 空闲时至少保持 5 个连接
      connection-timeout: 3000         # 获取连接超时时间（3 秒）

  data:
    redis:
      host: localhost                  # Redis 地址
      port: 6379                       # Redis 端口
      password:                        # Redis 密码（空 = 无密码）
      lettuce:
        pool:
          max-active: 50               # Redis 连接池最大连接数

  rabbitmq:                            # RabbitMQ 消息队列配置
    host: localhost
    port: 5672                         # AMQP 协议端口
    username: guest
    password: guest
    listener:
      simple:
        prefetch: 10                   # 每个消费者一次预取 10 条消息
        concurrency: 3                 # 同时有 3 个消费者线程
        acknowledge-mode: manual       # 手动确认模式（消费者处理成功后才 ACK）

  jackson:                             # JSON 序列化配置
    date-format: yyyy-MM-dd HH:mm:ss   # 日期格式
    time-zone: Asia/Shanghai           # 时区
    default-property-inclusion: non_null  # null 值字段不返回给前端

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true # 自动转换：数据库字段 user_name → Java 字段 userName
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 控制台打印 SQL（开发用）
  global-config:
    db-config:
      id-type: auto                    # 主键自增策略

jwt:
  secret: seckill-platform-jwt-secret-key-2024-this-is-a-very-long-secret-for-hs256  # JWT 签名密钥
  expiration: 604800000                # Token 有效期：7 天（毫秒）
```

---

### 1.6 小结：模块 0 你学到了什么

1. 数据库有 4 张表，对应 Java 里 4 个 Entity 类
2. 每个 Entity 对应一个 Mapper 接口，继承 `BaseMapper` 就自动获得增删改查能力
3. `SeckillApplication.java` 是项目的开关
4. `application.yml` 是项目的中枢配置
5. `@Data`、`@TableName`、`@Mapper` 等注解是给框架看的"标签"

**验证**：运行 `cd backend && ./mvnw compile`，看到 `BUILD SUCCESS` 即可。

---

## 第二章：模块 1 —— 用户注册（前后端联动）

> **目标**：实现第一个完整功能 —— 用户在网页上填写用户名密码，点击注册，数据存入数据库。
>
> **你将学到**：DTO 是什么、Service 层为什么存在、Controller 如何接收请求、Vue 表单如何发送请求。

### 2.0 先想清楚：注册这个动作发生了什么

在我们写任何代码之前，先把整个过程用大白话描述一遍：

```
1. 用户在浏览器里看到一个注册表单
2. 用户输入 用户名、密码、手机号（选填）、邮箱（选填）
3. 用户点击"注册"按钮
4. 前端把表单数据打包成 JSON
5. 前端发送 POST 请求到 http://localhost:8080/api/auth/register
6. 后端收到 JSON，转成 Java 对象
7. 后端检查：这个用户名有没有被注册过？
8. 如果已存在 → 返回 "用户名已存在"
9. 如果不存在 → BCrypt 加密密码，存入数据库
10. 返回成功提示
11. 前端收到成功响应，显示"注册成功"
```

这个流程里，后端需要 6 种文件配合工作。下面逐个创建并解释。

---

### 2.1 后端：DTO —— 前端发给后端的数据"包裹"

**DTO = Data Transfer Object（数据传输对象）**

它的唯一用途就是 **定义前端发给后端的 JSON 长什么样**。

**注册接口的前端 JSON**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000",
  "email": "abc@example.com"
}
```

这个 JSON 对应后端的 DTO 类。打开已有的 [dto/RegisterDTO.java](backend/src/main/java/com/seckill/dto/RegisterDTO.java)：

```java
package com.seckill.dto;          // DTO 类都在 dto 包下

import lombok.Data;               // 引入 @Data 注解

@Data                              // 自动生成 getter/setter/toString 等方法
public class RegisterDTO {         // 类名 RegisterDTO = Register（注册） + DTO
    private String username;       // 对应 JSON 里的 "username"
    private String password;       // 对应 JSON 里的 "password"（明文）
    private String phone;          // 对应 JSON 里的 "phone"（可选）
    private String email;          // 对应 JSON 里的 "email"（可选）
}
```

**关键理解**：

- DTO **不存数据库**，它只是前端和后端之间传递数据的"包裹"
- `@Data` 自动生成了 `getUsername()`, `setUsername()` 等方法，Spring 用这些方法把 JSON 字段值赋给 Java 对象
- 字段名必须和 JSON 的 key 完全一致（严格区分大小写）

---

### 2.2 后端：Service 层 —— 业务逻辑的大脑

**Service 是项目里最重要的一层，所有业务逻辑都写在这里。**

Spring 的惯例是"接口 + 实现"分离。为什么？因为：
1. **定义契约**：接口说清楚"我能做什么"
2. **方便替换**：测试时可以换一个假的实现
3. **Spring 的特性**：Spring 根据接口类型自动注入实现类

**接口**：[service/UserService.java](backend/src/main/java/com/seckill/service/UserService.java)

```java
package com.seckill.service;

import com.seckill.dto.RegisterDTO;
import com.seckill.dto.LoginDTO;

public interface UserService {       // interface = 只声明方法，不写实现代码
    void register(RegisterDTO dto);  // 注册方法：传入注册信息，无返回值（失败抛异常）
    String login(LoginDTO dto);      // 登录方法：传入用户名密码，成功返回 JWT 字符串
}
```

**实现类**：[service/impl/UserServiceImpl.java](backend/src/main/java/com/seckill/service/impl/UserServiceImpl.java)

这是真正干活的地方。让我们逐行读 `register` 方法：

```java
@Service                      // @Service：告诉 Spring 这是业务层组件，请管理它
@RequiredArgsConstructor      // 为 final 字段自动生成构造函数（Spring 通过它注入依赖）
public class UserServiceImpl implements UserService {  // implements = 实现 UserService 接口

    private final UserMapper userMapper;          // 注入：操作 user 表的 Mapper
    private final PasswordEncoder passwordEncoder; // 注入：BCrypt 密码加密器
    private final JwtUtils jwtUtils;               // 注入：JWT 工具类（登录时用）

    @Override                               // 表示这个方法是实现接口中定义的方法
    public void register(RegisterDTO dto) { // dto = 从 Controller 传过来的注册信息

        // 第1步：检查用户名是否已存在
        // selectCount()：查符合条件的记录数（SELECT COUNT(*) FROM user WHERE username=?）
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>()              // LambdaQueryWrapper：链式构造 WHERE 条件
                .eq(User::getUsername, dto.getUsername()) // .eq = equals，WHERE username = '传入的用户名'
        );                                               // User::getUsername 是方法引用，比手写 "username" 更安全
        if (count > 0) {                                // 同名用户已存在
            throw new BusinessException("用户名已存在");   // 抛出业务异常（见下一节异常处理）
        }

        // 第2步：创建 User 对象并保存
        User user = new User();                         // 新建空的 User 对象（相当于 user 表的一个空白行）
        user.setUsername(dto.getUsername());             // 设置用户名
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 重点！BCrypt 加密后存库，不能存明文
        user.setPhone(dto.getPhone());                   // 设置手机号
        user.setEmail(dto.getEmail());                   // 设置邮箱
        userMapper.insert(user);                         // 调用 MyBatis-Plus 的 insert → INSERT INTO user ...
    }
    
    // login 方法在模块 2（登录）中讲解
}
```

**每一行详解**：

| 代码 | 解释 |
|------|------|
| `@Service` | 告诉 Spring：这个类是 Service，启动时自动创建一个单例放进"对象仓库" |
| `@RequiredArgsConstructor` | Lombok 注解：为所有 `final` 字段生成构造函数 |
| `private final UserMapper userMapper;` | 声明一个不可变的 UserMapper 字段，Spring 通过构造函数自动注入 |
| `implements UserService` | 实现 UserService 接口，必须重写（Override）接口里声明的所有方法 |
| `new LambdaQueryWrapper<User>()` | 创建一个查询条件构造器，泛型 `<User>` 指明查询的是 user 表 |
| `.eq(User::getUsername, dto.getUsername())` | 等于条件：`WHERE username = ?`。`User::getUsername` 是 Lambda 方法引用 |
| `throw new BusinessException(...)` | 抛出异常来中断执行，比 `return` 更干净 |
| `passwordEncoder.encode(...)` | BCrypt 加密：同一密码每次生成结果不同（自动加盐） |
| `userMapper.insert(user)` | 把 user 对象插入数据库 |

---

### 2.3 后端：异常处理 —— 出了错怎么办

当 `throw new BusinessException("用户名已存在")` 被执行时，如果没有异常处理器，用户会看到一个丑陋的 500 错误页面。

所以我们需要两块代码：自定义异常类 + 全局异常处理器。

**自定义异常**：[exception/BusinessException.java](backend/src/main/java/com/seckill/exception/BusinessException.java)

```java
public class BusinessException extends RuntimeException {  // 继承运行时异常（不需要 try-catch）
    private final int code;          // 错误码

    public BusinessException(String message) {  // 构造函数1：只传消息，默认 code=400
        super(message);              // 调用父类构造函数，保存消息
        this.code = 400;             // HTTP 400 = Bad Request
    }

    public BusinessException(int code, String message) { // 构造函数2：自定义错误码
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }  // 获取错误码
}
```

**全局异常处理器**：[exception/GlobalExceptionHandler.java](backend/src/main/java/com/seckill/exception/GlobalExceptionHandler.java)

```java
@Slf4j                         // Lombok：生成 log 对象，用于打印日志
@RestControllerAdvice          // 核心：全局拦截所有 Controller 抛出的异常
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)  // 只捕获 BusinessException
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());  // 记录日志
        return Result.fail(e.getCode(), e.getMessage());     // 返回友好 JSON
    }

    @ExceptionHandler(DuplicateKeyException.class)  // 捕获数据库唯一约束冲突
    @ResponseStatus(HttpStatus.CONFLICT)            // HTTP 409
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("Duplicate key: {}", e.getMessage());
        return Result.fail(409, "重复操作");
    }

    @ExceptionHandler(Exception.class)              // 兜底：处理所有未捕获的异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return Result.fail(500, "服务器内部错误");
    }
}
```

**工作原理**：

```
Controller 执行中 → 抛 BusinessException → 被 @ExceptionHandler 捕获 → 自动返回 Result JSON
                        ↓（未被捕获的）→ 到达兜底 Exception.class → 返回 500 JSON
```

---

### 2.4 后端：Result —— 统一响应格式

前端不喜欢每个接口返回格式不一样。所以我们约定：**所有接口返回 `{code, message, data}`**。

打开 [vo/Result.java](backend/src/main/java/com/seckill/vo/Result.java)：

```java
@Data
public class Result<T> {             // <T> 是泛型：data 可以是任意类型
    private int code;                // 状态码：200=成功, 400=业务错, 401=未登录, 500=服务器错
    private String message;          // 提示信息
    private T data;                  // 数据（泛型）

    // 静态工厂方法：方便使用
    public static <T> Result<T> ok(T data) {        // 成功+有数据：Result.ok(userList)
        return new Result<>(200, "success", data);
    }
    public static <T> Result<T> ok() {              // 成功+无数据：Result.ok()
        return new Result<>(200, "success", null);
    }
    public static <T> Result<T> fail(String message) {      // 失败：Result.fail("用户名已存在")
        return new Result<>(500, message, null);
    }
    public static <T> Result<T> fail(int code, String message) { // 失败+自定义码
        return new Result<>(code, message, null);
    }
}
```

**使用方式**（在 Controller 里）：
```java
Result.ok()                  → { "code": 200, "message": "success", "data": null }
Result.ok(token)             → { "code": 200, "message": "success", "data": "eyJhbGci..." }
Result.fail("用户名已存在")    → { "code": 500, "message": "用户名已存在", "data": null }
Result.fail(401, "未登录")    → { "code": 401, "message": "未登录", "data": null }
```

---

### 2.5 后端：Controller —— 前后端对接的大门

Controller 是后端最"外面"的一层，直接和前端 HTTP 请求打交道。

打开 [controller/AuthController.java](backend/src/main/java/com/seckill/controller/AuthController.java)：

```java
@RestController              // = @Controller + @ResponseBody：所有方法返回值自动转 JSON
@RequestMapping("/api/auth") // 这个 Controller 处理以 /api/auth 开头的请求
@RequiredArgsConstructor     // 自动注入 userService
public class AuthController {

    private final UserService userService;  // 注入业务层（Controller 不写逻辑，只调用 Service）

    @PostMapping("/register")              // 处理 POST /api/auth/register
    public Result<?> register(@RequestBody RegisterDTO dto) {  // @RequestBody：JSON → DTO 对象
        userService.register(dto);          // 调用业务层
        return Result.ok();                 // 返回 { "code":200, "message":"success", "data":null }
    }

    @PostMapping("/login")                 // 处理 POST /api/auth/login（模块 2 讲解）
    public Result<String> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return Result.ok(token);           // 返回 { "code":200, "data":"eyJhbGci..." }
    }
}
```

**逐行解释**：

| 注解/代码 | 大白话 |
|-----------|--------|
| `@RestController` | "我返回的不是 HTML 页面，是 JSON 字符串" |
| `@RequestMapping("/api/auth")` | "所有方法路径的前缀是 /api/auth" |
| `@PostMapping("/register")` | "完整路径 = /api/auth + /register = /api/auth/register，只接受 POST 请求" |
| `@RequestBody` | "把前端发来的 JSON 字符串自动转换成 RegisterDTO 对象" |
| `Result<?>` | 返回值类型 `Result`，`?` 表示 data 可以是任意类型 |

---

### 2.6 后端：BeanConfig —— 告诉 Spring 怎么创建 BCrypt 对象

Spring 能自动管理大部分对象，但有些需要手动配置。

打开 [config/BeanConfig.java](backend/src/main/java/com/seckill/config/BeanConfig.java)：

```java
@Configuration               // 标记为配置类（Spring 启动时优先读取）
public class BeanConfig {

    @Bean                    // 这个方法的返回值会变成 Spring 管理的 Bean
    public PasswordEncoder passwordEncoder() {   // 方法名 = Bean 的名字
        return new BCryptPasswordEncoder();      // 返回 BCrypt 实现
    }
}
```

**为什么需要这个？**

`PasswordEncoder` 是一个接口，BCryptPasswordEncoder 只是一个实现。Spring 自己不知道你要用哪种加密算法，所以需要你在 Config 里明确指定。

之后在任何 Service 里写 `private final PasswordEncoder passwordEncoder;`，Spring 就会自动注入这个 BCryptPasswordEncoder。

---

### 2.7 后端小结 + 验证

你已经知道注册功能用到的后端文件：

```
RegisterDTO.java          ← 前端数据的"包裹"
UserService.java          ← 定义注册登录的"合同"
UserServiceImpl.java      ← 真正干活的代码
BusinessException.java    ← 自定义错误类型
GlobalExceptionHandler.java ← 全局错误拦截
Result.java               ← 统一响应格式
AuthController.java       ← 接收 HTTP 请求的大门
BeanConfig.java           ← 手动创建 BCrypt 对象
UserMapper.java           ← 操作数据库的遥控器
User.java                 ← 数据库表的映射
```

**验证你的后端能跑：**

```bash
cd backend && ./mvnw spring-boot:run
```

启动成功后，另开一个终端测试注册：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","phone":"13800138000"}'
```

返回 `{"code":200,"message":"success","data":null}` 表示成功。去数据库查一下：

```sql
SELECT * FROM seckill.user;
```

应该看到 `password` 字段是 `$2a$10$...` 开头的密文。

---

### 2.8 前端：初始化 Vue 3 项目

后端能接收请求了，现在让浏览器也能发送请求。

```bash
cd /home/ydmy/seckill-platform
npm create vite@5 frontend -- --template vue
cd frontend
npm install
npm install element-plus @element-plus/icons-vue axios pinia vue-router@4
```

**这些包分别是干什么的？**

| 包 | 作用 |
|----|------|
| `vue` | Vue 3 框架（已含在模板中） |
| `vite` | 前端构建工具（替代 webpack，更快） |
| `element-plus` | UI 组件库（按钮、表单、表格、弹窗等，相当于 Ant Design） |
| `@element-plus/icons-vue` | Element Plus 的图标库 |
| `axios` | 发 HTTP 请求（相当于浏览器版 curl） |
| `pinia` | Vue 3 的状态管理库（存用户登录状态等，替代 Vuex） |
| `vue-router` | 前端路由（页面跳转，不用刷新浏览器） |

---

### 2.9 前端：配置 Vite 代理

前端跑在 `localhost:3000`，后端跑在 `localhost:8080`。浏览器的**同源策略**会阻止跨域请求。解决方案：Vite 开发代理。

修改 `frontend/vite.config.js`：

```javascript
import { defineConfig } from 'vite'       // Vite 的配置函数
import vue from '@vitejs/plugin-vue'      // Vite 的 Vue 3 插件（处理 .vue 文件）

export default defineConfig({
  plugins: [vue()],                       // 注册 Vue 插件
  server: {
    port: 3000,                           // 前端开发服务器跑在 3000 端口
    proxy: {
      '/api': {                           // 所有以 /api 开头的请求
        target: 'http://localhost:8080',  // 转发到后端 8080 端口
        changeOrigin: true                // 修改请求头的 Origin 字段
      }
    }
  }
})
```

**工作原理**：
- 前端页面发请求 `GET /api/products`
- Vite Dev Server 拦截到 `/api` 开头，改成请求 `http://localhost:8080/api/products`
- 后端返回 JSON，Vite 再传回给浏览器
- 浏览器以为自己一直在和 `localhost:3000` 通信（骗过同源策略）

---

### 2.10 前端：创建目录结构

在 `frontend/src/` 下创建以下目录：

```bash
mkdir -p frontend/src/api         # API 请求函数
mkdir -p frontend/src/store       # Pinia 状态管理
mkdir -p frontend/src/views       # 页面组件
mkdir -p frontend/src/router      # 路由配置
```

---

### 2.11 前端：Axios 封装 —— 统一的 HTTP 请求工具

新建 `frontend/src/api/request.js`：

```javascript
import axios from 'axios'                    // 引入 axios
import { ElMessage } from 'element-plus'     // Element Plus 的消息提示组件（弹窗用）

// 创建一个配置好的 axios 实例（相当于 axios 的"分身"，带默认配置）
const request = axios.create({
  baseURL: '/api',              // 所有请求自动加上 /api 前缀（配合 Vite 代理）
  timeout: 10000                // 请求超时时间：10 秒没响应就报错
})

// ========== 请求拦截器：在请求发出之前执行 ==========
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 中取出登录时保存的 token
    const token = localStorage.getItem('token')
    if (token) {
      // 如果 token 存在，把它塞到请求头里（后端 JwtInterceptor 从这读取）
      config.headers.Authorization = `Bearer ${token}`
    }
    return config  // 必须 return config，否则请求发不出去
  },
  (error) => {
    return Promise.reject(error)  // 把错误继续往下传
  }
)

// ========== 响应拦截器：在收到响应之后、交给你代码之前执行 ==========
request.interceptors.response.use(
  (response) => {
    const res = response.data          // 取后端返回的 JSON：{ code, message, data }
    if (res.code !== 200) {            // 如果状态码不是 200（成功），说明有错误
      ElMessage.error(res.message || '请求失败')  // 弹红色错误提示
      return Promise.reject(new Error(res.message))  // 把错误往下传
    }
    return res  // 成功的话，返回 { code, message, data }
  },
  (error) => {
    // HTTP 层面的错误（网络断开、超时、后端挂了等）
    if (error.response) {
      if (error.response.status === 401) {       // 401 = 未登录/Token 过期
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')         // 清除过期的 token
        window.location.href = '/login'          // 跳转到登录页
      } else {
        ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
```

**拦截器的意义**：
- 请求拦截器统一加 `Authorization` 头，不用在每个接口里重复写
- 响应拦截器统一处理错误，不用在每个页面里写 `if (res.code !== 200)`

---

### 2.12 前端：API 层 —— 定义每个接口怎么调用

新建 `frontend/src/api/auth.js`：

```javascript
import request from './request.js'     // 引入刚才封装好的 axios 实例

// 注册接口：传用户名密码，返回 { code, message, data }
export function register(data) {
  return request.post('/auth/register', data)   // POST /api/auth/register
}

// 登录接口：传用户名密码，返回 { code, message, data: "jwt字符串" }
export function login(data) {
  return request.post('/auth/login', data)      // POST /api/auth/login
}
```

**注意**：这里的路径 `/auth/register` 会被 `request.js` 里的 `baseURL: '/api'` 自动拼接成 `/api/auth/register`。

---

### 2.13 前端：Pinia Store —— 管理用户登录状态

新建 `frontend/src/store/user.js`：

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

// defineStore('user', ...) → 创建一个叫 "user" 的 store
export const useUserStore = defineStore('user', () => {
  // ===== 状态（相当于 data）=====
  const token = ref(localStorage.getItem('token') || '')  // 从 localStorage 恢复 token（刷新不丢失）
  const username = ref('')                                  // 当前登录的用户名

  // ===== 动作（相当于 methods）=====
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)   // 存 localStorage，刷新页面还能取到
  }

  function setUsername(name) {
    username.value = name
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')          // 退出登录时清除 token
  }

  // ===== 计算属性（相当于 computed）=====
  // isLoggedIn 不是方法，是一个根据 token 自动计算的值
  // const isLoggedIn = computed(() => !!token)

  // 把需要给外部用的数据和方法暴露出去
  return { token, username, setToken, setUsername, logout }
})
```

---

### 2.14 前端：Router —— 控制页面跳转

新建 `frontend/src/router/index.js`：

```javascript
import { createRouter, createWebHistory } from 'vue-router'

// createWebHistory() → 使用 HTML5 History 模式（URL 里没有 # 号，看起来像正常 URL）
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',                    // URL 路径
      name: 'Login',                     // 路由名字（编程式跳转时用）
      component: () => import('../views/Login.vue')    // 懒加载：访问时才加载这个页面的代码
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/',                         // 首页（暂时先重定向到注册页）
      redirect: '/register'
    }
  ]
})

export default router
```

---

### 2.15 前端：配置 main.js 入口文件

修改 `frontend/src/main.js`，引入 Element Plus、Router、Pinia：

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'      // Element Plus 的样式文件
import * as ElementPlusIconsVue from '@element-plus/icons-vue'  // 所有图标
import App from './App.vue'
import router from './router/index.js'

const app = createApp(App)

app.use(createPinia())                    // 注册 Pinia（状态管理）
app.use(router)                           // 注册 Router（路由跳转）
app.use(ElementPlus)                      // 注册 Element Plus（UI 组件库）

// 注册所有图标组件（这样在 .vue 文件里可以直接用 <el-icon><UserFilled /></el-icon>）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')                         // 挂载到 index.html 的 <div id="app">
```

修改 `frontend/src/App.vue`（根组件，只留路由出口）：

```html
<template>
  <router-view />     <!-- 这个位置会根据 URL 自动显示对应的页面组件 -->
</template>

<script setup>
// App.vue 只负责"框架"，具体页面内容由 router-view 动态切换
</script>
```

---

### 2.16 前端：注册页面

新建 `frontend/src/views/Register.vue`：

```html
<template>
  <div class="register-container">
    <!-- el-card：Element Plus 的卡片组件，自带边框和阴影 -->
    <el-card class="register-card" title="用户注册">
      <h2>用户注册</h2>

      <!-- el-form：表单组件 -->
      <!-- :model="form" → 表单数据绑定到 form 对象 -->
      <!-- label-width="80px" → 标签宽度 80 像素 -->
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <!-- v-model：双向绑定，输入框的值和 form.username 自动同步 -->
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>

        <el-form-item>
          <!-- type="primary" → 蓝色按钮 -->
          <!-- @click="handleRegister" → 点击时调用 handleRegister 方法 -->
          <el-button type="primary" @click="handleRegister" :loading="loading">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
          <!-- router-link：用 router 跳转，不会刷新页面 -->
          <router-link to="/login">
            <el-button type="text">已有账号？去登录</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
// ===== script setup：Vue 3 Composition API 的语法糖 =====
import { ref, reactive } from 'vue'       // ref: 单一响应式数据, reactive: 对象响应式
import { useRouter } from 'vue-router'    // 编程式路由跳转
import { ElMessage } from 'element-plus'  // 消息弹窗
import { register } from '../api/auth.js' // 引入注册接口函数

const router = useRouter()                // 获取路由实例（用来跳转页面）

const loading = ref(false)                // ref(false) = 创建一个初始值为 false 的响应式变量
                                          // 为 true 时按钮显示"注册中..."并禁用

// reactive({...}) = 创建一个响应式对象，里面的任何字段变化，页面自动更新
const form = reactive({
  username: '',     // 绑定到输入框
  password: '',
  phone: '',
  email: ''
})

// async：异步函数，里面可以用 await 等待
const handleRegister = async () => {
  // 简单校验：用户名和密码不能为空
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return   // 不满足条件就不往下执行
  }

  loading.value = true          // 显示加载状态
  try {
    await register(form)        // 调用 API：发 POST 请求到后端注册接口
    ElMessage.success('注册成功！')  // 绿色成功提示
    router.push('/login')       // 跳转到登录页
  } catch (error) {
    // 错误已经在 request.js 的拦截器里提示过了，这里可选加额外处理
  } finally {
    loading.value = false       // 无论成功失败，都关闭加载状态
  }
}
</script>

<style scoped>
/* scoped：这个样式只对当前组件生效，不会影响其他页面 */
.register-container {
  display: flex;
  justify-content: center;    /* 水平居中 */
  align-items: center;        /* 垂直居中 */
  height: 100vh;              /* 占满整个视口高度 */
  background: #f5f5f5;
}
.register-card {
  width: 420px;               /* 卡片宽度 */
}
.register-card h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>
```

**Vue 3 关键语法解释**：

| 代码 | 含义 |
|------|------|
| `ref(false)` | 创建一个响应式数据。`.value` 读写值，模板里自动解包，直接用 `loading` |
| `reactive({...})` | 创建一个响应式对象。直接 `.属性名` 读写，如 `form.username` |
| `v-model="form.username"` | 双向绑定：输入框的值变化 → `form.username` 跟着变；反之亦然 |
| `@click="handleRegister"` | 点击事件绑定，等于 `v-on:click` |
| `:loading="loading"` | 动态属性绑定，`:loading` 是 `v-bind:loading` 的缩写 |
| `async / await` | 异步操作的同步写法。`await register(form)` 等价于 `.then(res => ...)` |
| `router.push('/login')` | 编程式路由跳转，和 `<router-link>` 效果一样 |

---

### 2.17 前端：登录页面

新建 `frontend/src/views/Login.vue`：

```html
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password
            @keyup.enter="handleLogin" />   <!-- 按回车键也可以提交 -->
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
          <router-link to="/register">
            <el-button type="text">没有账号？去注册</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth.js'          // 引入登录接口函数
import { useUserStore } from '../store/user.js'  // 引入 Pinia store

const router = useRouter()
const userStore = useUserStore()    // 获取 user store 实例

const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }

  loading.value = true
  try {
    const res = await login(form)           // 调用登录 API
    userStore.setToken(res.data)            // 把返回的 JWT 存到 Pinia + localStorage
    userStore.setUsername(form.username)    // 保存用户名
    ElMessage.success('登录成功！')
    router.push('/')                        // 跳转到首页（目前会重定向到注册页，后面改）
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}
.login-card {
  width: 420px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>
```

---

### 2.18 对接验证：前后端连通测试

1. **启动后端**：
   ```bash
   cd backend && ./mvnw spring-boot:run
   ```

2. **启动前端**：
   ```bash
   cd frontend && npm run dev
   ```

3. **打开浏览器** `http://localhost:3000/register`

4. **测试注册**：输入用户名 `test`、密码 `123456`，点击注册 → 看到绿色"注册成功"提示 → 跳转到登录页

5. **测试登录**：输入刚才注册的用户名密码，点击登录 → 看到绿色"登录成功"提示

6. **验证 token**：打开浏览器 F12 → Application → Local Storage → 看到 `token` 字段，值是一长串 JWT 字符串

7. **验证数据库**：
   ```sql
   SELECT * FROM user;
   ```
   看到 `test` 用户，password 字段是 `$2a$10$...` 密文。

**恭喜！你完成了第一个完整的前后端功能。**

---

## 第三章：模块 2 —— 用户登录 + JWT（前后端联动）

> **目标**：实现登录功能，后端返回 JWT Token，前端保存 Token，后续所有 API 请求自动携带 Token。
>
> **你将学到**：JWT 是什么、拦截器如何工作、Axios 拦截器如何自动加 Token、前端路由守卫。

### 3.0 JWT 是什么？为什么用它？

**JWT = JSON Web Token**，一个经过签名的字符串，里面可以存少量数据。

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzQwMDAwMDAwfQ.signature
│                      │                                   │
│                      │                                   └── 签名（防篡改）
│                      └── 载荷（存的数据，如用户ID）
└── 头部（签名算法）
```

**为什么不直接用 Session？**

| Session（传统） | JWT（本项目） |
|-----------------|-------------|
| 用户信息存在服务端内存 | 用户信息存在 Token 里 |
| 多台服务器需要 Session 同步 | 无状态，任何服务器都能验证 |
| 服务端需要查 Session 存储 | 服务端直接解密验证，不查存储 |

简单的说：**JWT 让后端不用"记住"谁登录了，每次请求带 Token 过来直接验就行。**

---

### 3.1 后端：JWT 工具类

打开已有的 [common/JwtUtils.java](backend/src/main/java/com/seckill/common/JwtUtils.java)，理解三个核心方法：

```java
@Component                   // Spring 管理的组件
public class JwtUtils {

    private final SecretKey key;    // HMAC-SHA256 签名密钥（从 application.yml 读取 secret 字段生成）
    private final long expiration;  // Token 过期时间（7天）

    // 构造函数：@Value 从 application.yml 读取配置值
    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // 生成 Token：把 userId 编码进去，设置过期时间，用密钥签名
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);  // 7天后过期
        return Jwts.builder()
                .subject(String.valueOf(userId))   // sub 字段存用户 ID
                .issuedAt(now)                     // iat 签发时间
                .expiration(expiryDate)            // exp 过期时间
                .signWith(key)                     // 签名（防篡改）
                .compact();                        // 生成最终字符串
    }

    // 从 Token 中提取 userId
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());  // 取出 sub 字段
    }

    // 验证 Token 是否有效（签名正确 + 未过期）
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;  // 解析成功 = 有效
        } catch (JwtException e) {
            return false;  // 签名不对/过期/格式错误 = 无效
        }
    }
}
```

---

### 3.2 后端：登录的 Service 逻辑

在 [UserServiceImpl.java](backend/src/main/java/com/seckill/service/impl/UserServiceImpl.java) 中已有 `login` 方法：

```java
@Override
public String login(LoginDTO dto) {
    // 第1步：根据用户名查用户
    User user = userMapper.selectOne(
        new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
    );
    if (user == null) {
        throw new BusinessException("用户名或密码错误");  // 不存在这个用户
    }

    // 第2步：验证密码
    // matches(明文, BCrypt密文)：比对是否匹配，不需要知道原始密码
    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        throw new BusinessException("用户名或密码错误");  // 密码不对
    }

    // 第3步：生成 JWT Token 并返回
    return jwtUtils.generateToken(user.getId());
}
```

**安全提示**：错误消息统一写"用户名或密码错误"，不区分是哪个错了。如果明确说"用户名不存在"，攻击者就能枚举出哪些用户名已注册。

---

### 3.3 后端：JWT 拦截器 —— 给 API 加门禁

注册和登录不需要登录就能访问。但以后的所有接口（查订单、秒杀等）都需要登录。

JWT 拦截器就是"门禁系统"：**每个请求到 Controller 之前，先检查有没有合法的 Token**。

打开 [interceptor/JwtInterceptor.java](backend/src/main/java/com/seckill/interceptor/JwtInterceptor.java)：

```java
@Component                                         // Spring 组件
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {  // 实现拦截器接口

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,    // 请求对象
                             HttpServletResponse response,  // 响应对象
                             Object handler) {              // 将被调用的 Controller 方法

        // 第1步：OPTIONS 预检请求直接放行（浏览器跨域机制需要）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;  // true = 放行
        }

        // 第2步：从请求头拿 Authorization 字段（前端 Axios 拦截器会设置）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);                    // HTTP 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;  // false = 拦截，请求到此为止，Controller 不会执行
        }

        // 第3步：去掉 "Bearer " 前缀，得到纯净的 Token 字符串
        String token = authHeader.substring(7);

        // 第4步：验证 Token
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期\"}");
            return false;
        }

        // 第5步：从 Token 中取出 userId，存到 request 的属性里
        // 后面的 Controller 通过 request.getAttribute("userId") 就能拿到
        Long userId = jwtUtils.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        return true;  // 放行
    }
}
```

**拦截器的工作流**：

```
请求进来 → WebConfig 判断路径是否需要拦截
  ├── 路径是 /api/auth/login → 不需要，直接到 Controller
  └── 路径是 /api/orders → 需要，进入 JwtInterceptor.preHandle()
        ├── Token 不存在 → 返回 401 JSON，结束
        ├── Token 过期 → 返回 401 JSON，结束
        └── Token 有效 → 提取 userId，放行到 Controller
```

---

### 3.4 后端：WebConfig —— 注册拦截器 + 跨域配置

打开 [config/WebConfig.java](backend/src/main/java/com/seckill/config/WebConfig.java)：

```java
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    // 跨域配置：允许前端 localhost:3000 调用后端 localhost:8080
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                     // 对所有路径生效
                .allowedOriginPatterns("*")             // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")                    // 允许所有请求头（包括 Authorization）
                .allowCredentials(true);                // 允许携带 Cookie
    }

    // 拦截器注册：哪些路径需要登录，哪些不需要
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)          // 注册 JWT 拦截器
                .addPathPatterns("/api/**")               // 拦截所有 /api/ 路径
                .excludePathPatterns(                     // 排除（不拦截）这些路径：
                        "/api/auth/login",                // 登录
                        "/api/auth/register",             // 注册
                        "/api/products",                  // 商品列表（暂时公开）
                        "/api/products/*/seckill"         // 秒杀详情（暂时公开）
                );
    }
}
```

---

### 3.5 前端：路由守卫 —— 没登录不能访问

更新 `frontend/src/router/index.js`：

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
    { path: '/', redirect: '/register' }
  ]
})

// ===== 全局前置守卫：每次路由跳转前执行 =====
router.beforeEach((to, from, next) => {
  // 从 localStorage 检查是否有 token
  const token = localStorage.getItem('token')

  if (token) {
    // 有 token → 已登录，可以访问任何页面
    // 如果用户已经登录还想访问登录页，直接跳到首页（后面会改成商品列表）
    if (to.path === '/login' || to.path === '/register') {
      next('/')  // 重定向到首页
    } else {
      next()     // 正常放行
    }
  } else {
    // 没有 token → 未登录
    // 登录页和注册页不用登录就能访问
    if (to.path === '/login' || to.path === '/register') {
      next()     // 放行
    } else {
      next('/login')  // 其他页面：强制跳转到登录页
    }
  }
})

export default router
```

---

### 3.6 前后端登录对接流程全览

```
用户在前端登录页面输入用户名密码，点击登录
    │
    ▼
Login.vue 调用 auth.js 的 login()
    │
    ▼
auth.js 调用 request.post('/auth/login', form)
    │
    ▼
request.js 的请求拦截器：检查 localStorage → 无 token → 不加 Authorization 头
    │
    ▼
Vite 代理转发到 http://localhost:8080/api/auth/login
    │
    ▼
WebConfig：/api/auth/login 在排除列表中 → 不经过 JwtInterceptor → 直达 Controller
    │
    ▼
AuthController.login() → UserServiceImpl.login()
    │
    ▼
查数据库 → 校验 BCrypt 密码 → 生成 JWT → 返回 { code: 200, data: "eyJhbGci..." }
    │
    ▼
request.js 响应拦截器：code=200，返回 res
    │
    ▼
Login.vue：res.data 是 JWT 字符串 → userStore.setToken(res.data) 存入 localStorage
    │
    ▼
Router 导航守卫检测到 token → 允许跳转首页
```

---

### 3.7 验证登录功能

1. **先注册一个用户**（用上一章的注册页或用 curl）
2. **登录**：浏览器打开 `http://localhost:3000/login`，输入用户名密码，点击登录
3. **打开 F12 → Application → Local Storage**，看到 `token` 字段
4. **复制这个 token**，去 [jwt.io](https://jwt.io) 粘贴，可以看到里面存了什么数据（sub 字段就是 userId）
5. **测试保护接口**（此时没有任何受保护接口，下一章有商品管理后就能测）

---

### 3.8 模块 2 小结

后端新增/修改的文件：

```
LoginDTO.java             ← 前端登录表单的"包裹"（新建）
JwtUtils.java             ← JWT 签名/验证/解析工具（已有）
UserServiceImpl.login()   ← 登录业务逻辑（补完）
AuthController.login()    ← 登录接口（补完）
JwtInterceptor.java       ← 请求门禁（新建）
WebConfig.java            ← 注册拦截器 + 跨域配置（新建）
```

前端新建的文件：

```
src/api/request.js        ← Axios 封装 + 拦截器
src/api/auth.js           ← 登录/注册 API 函数
src/store/user.js         ← Pinia 管理用户状态和 token
src/router/index.js       ← 路由 + 导航守卫
src/views/Register.vue    ← 注册页面
src/views/Login.vue       ← 登录页面
src/main.js               ← 注册 Element Plus + Router + Pinia（修改）
src/App.vue               ← 根组件（修改）
vite.config.js            ← 配置代理（修改）
```

---

## 第四章：模块 3 —— 商品管理（前后端联动）

> **目标**：实现商品列表和秒杀活动列表的展示，用户可以浏览商品、查看秒杀活动详情。
>
> **你将学到**：分页查询怎么做、关联查询怎么处理、Vue 如何展示商品卡片列表、前端路由如何组织多个页面。

### 4.0 先想清楚：商品模块要做什么

```
1. 用户打开网站首页 → 看到商品卡片列表
2. 用户看到一个"秒杀专场"入口 → 看到正在进行的秒杀活动
3. 用户点击某个秒杀活动 → 看到秒杀详情（价格、库存、倒计时）
4. 管理员可以在后台添加商品、配置秒杀活动
```

这个模块里，你需要创建 **2 个 Service + 2 个 Controller**，然后写前端商品列表页。

---

### 4.1 后端：ProductService —— 商品查询逻辑

新建 [service/ProductService.java](backend/src/main/java/com/seckill/service/ProductService.java)：

```java
package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;

public interface ProductService {
    /** 分页查询上架商品列表（游客和登录用户都能看） */
    Page<Product> listProducts(int page, int size);
    /** 根据 ID 查单个商品 */
    Product getProductById(Long id);
}
```

新建 [service/impl/ProductServiceImpl.java](backend/src/main/java/com/seckill/service/impl/ProductServiceImpl.java)：

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> listProducts(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);  // 创建分页对象：第几页、每页几条
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)                  // 只查 status=1 的上架商品
                .orderByDesc(Product::getCreateTime);       // 按创建时间倒序（最新的在前面）
        return productMapper.selectPage(pageParam, wrapper); // MyBatis-Plus 自动执行 COUNT + LIMIT
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);  // 按主键查单个商品
    }
}
```

**新知识：MyBatis-Plus 分页**

`Page<Product> pageParam = new Page<>(1, 10)` 表示第 1 页、每页 10 条。传给 `selectPage()` 后，MyBatis-Plus 会：
1. 自动执行 `SELECT COUNT(*) FROM product WHERE status=1` — 查总数
2. 自动执行 `SELECT * FROM product WHERE status=1 ORDER BY create_time DESC LIMIT 0,10` — 查当页数据
3. 返回的 `Page` 对象里有 `getRecords()`（数据列表）、`getTotal()`（总数）、`getPages()`（总页数）

**但是**：MyBatis-Plus 分页需要配置插件才能生效。在 [config/MyBatisPlusConfig.java](backend/src/main/java/com/seckill/config/MyBatisPlusConfig.java) 中：

```java
@Configuration
@MapperScan("com.seckill.mapper")
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 分页插件
        return interceptor;
    }
}
```

---

### 4.2 后端：SeckillProductService —— 秒杀活动查询

新建 [service/SeckillProductService.java](backend/src/main/java/com/seckill/service/SeckillProductService.java)：

```java
package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.SeckillProduct;

public interface SeckillProductService {
    /** 查询当前可用的秒杀活动列表（状态=进行中，当前时间在开始和结束之间） */
    Page<SeckillProduct> listActiveSeckillProducts(int page, int size);

    /** 查单个秒杀活动详情（含关联的商品名、原价等） */
    SeckillProduct getSeckillProductById(Long seckillProductId);
}
```

新建 [service/impl/SeckillProductServiceImpl.java](backend/src/main/java/com/seckill/service/impl/SeckillProductServiceImpl.java)：

```java
@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl implements SeckillProductService {

    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper productMapper;  // 需要联合查商品表
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<SeckillProduct> listActiveSeckillProducts(int page, int size) {
        Page<SeckillProduct> pageParam = new Page<>(page, size);
        LocalDateTime now = LocalDateTime.now();
        // 三个条件：状态=进行中 + 已经开始(startTime <= now) + 还未结束(endTime >= now)
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<SeckillProduct>()
                .eq(SeckillProduct::getStatus, 1)
                .le(SeckillProduct::getStartTime, now)    // le = less than or equal, <=
                .ge(SeckillProduct::getEndTime, now)      // ge = greater than or equal, >=
                .orderByDesc(SeckillProduct::getCreateTime);

        Page<SeckillProduct> result = seckillProductMapper.selectPage(pageParam, wrapper);

        // 关键：填充关联的商品信息
        // SeckillProduct 里只存了 productId，但前端需要展示商品名称、图片、原价
        // @TableField(exist=false) 的字段 MyBatis-Plus 不会自动查，需要手动填充
        for (SeckillProduct sp : result.getRecords()) {
            Product product = productMapper.selectById(sp.getProductId());
            if (product != null) {
                sp.setProductName(product.getName());
                sp.setOriginalPrice(product.getOriginalPrice());
                sp.setImageUrl(product.getImageUrl());
            }
        }
        return result;
    }

    @Override
    public SeckillProduct getSeckillProductById(Long seckillProductId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
        if (sp != null) {
            Product product = productMapper.selectById(sp.getProductId());
            if (product != null) {
                sp.setProductName(product.getName());
                sp.setOriginalPrice(product.getOriginalPrice());
                sp.setImageUrl(product.getImageUrl());
            }
        }
        return sp;
    }
}
```

**理解这段代码的关键**：

`seckill_product` 表只存了 `product_id`，但前端展示需要商品名称、原价、图片。这些在 `product` 表里。所以查完秒杀活动后，再根据 `product_id` 去 `product` 表查出商品信息，手动 set 到 `SeckillProduct` 的 `@TableField(exist=false)` 字段上。

这是 **手动 JOIN 的简化版**，适合数据量不大的场景。真正的 JOIN 查询需要写自定义 SQL（后面会讲到）。

---

### 4.3 后端：ProductController —— 公开的商品接口

新建 [controller/ProductController.java](backend/src/main/java/com/seckill/controller/ProductController.java)：

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SeckillProductService seckillProductService;

    /** GET /api/products?page=1&size=10 — 商品列表（分页） */
    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,   // defaultValue：前端不传则默认第1页
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.listProducts(page, size));
    }

    /** GET /api/products/{id} — 单个商品详情 */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) { // @PathVariable：取 URL 路径中的 {id}
        Product product = productService.getProductById(id);
        if (product == null) return Result.fail("商品不存在");
        return Result.ok(product);
    }

    /** GET /api/products/seckill — 进行中的秒杀活动列表 */
    @GetMapping("/seckill")
    public Result<Page<SeckillProduct>> seckillList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(seckillProductService.listActiveSeckillProducts(page, size));
    }

    /** GET /api/products/seckill/{id} — 单个秒杀活动详情 */
    @GetMapping("/seckill/{seckillProductId}")
    public Result<SeckillProduct> seckillDetail(@PathVariable Long seckillProductId) {
        SeckillProduct sp = seckillProductService.getSeckillProductById(seckillProductId);
        if (sp == null) return Result.fail("秒杀活动不存在");
        return Result.ok(sp);
    }
}
```

**新注解**：
| 注解 | 大白话 |
|------|--------|
| `@RequestParam(defaultValue = "1")` | "前端不传这个参数的话，默认值是 1" |
| `@PathVariable` | "从 URL 路径中取值" (如 `/api/products/5` → `id=5`) |

**注意路由顺序**：`/seckill` 必须放在 `/{id}` 之前。因为 Spring 按顺序匹配路由，如果 `/{id}` 在前，`/seckill` 会被当成 `id="seckill"`。

---

### 4.4 后端：AdminProductController —— 管理员后台接口

新建 [controller/AdminProductController.java](backend/src/main/java/com/seckill/controller/AdminProductController.java)：

```java
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductMapper productMapper;           // 直接注入 Mapper（简单 CRUD 不需要 Service）
    private final SeckillProductMapper seckillProductMapper;

    @PostMapping("/products")                            // POST /api/admin/products — 新增商品
    public Result<Product> createProduct(@RequestBody Product product) {
        productMapper.insert(product);
        return Result.ok(product);
    }

    @PutMapping("/products/{id}")                        // PUT /api/admin/products/1 — 更新商品
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.ok(product);
    }

    @DeleteMapping("/products/{id}")                     // DELETE /api/admin/products/1 — 删除商品
    public Result<?> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/seckill-products")                    // POST /api/admin/seckill-products — 新增秒杀活动
    public Result<SeckillProduct> createSeckillProduct(@RequestBody SeckillProduct sp) {
        seckillProductMapper.insert(sp);
        return Result.ok(sp);
    }
}
```

> **什么时候直接注入 Mapper、什么时候注入 Service？** 简单 CRUD（增删改查，没有复杂业务逻辑）可以直接调 Mapper。有业务规则（比如"注册前要检查用户名是否已存在、要加密密码"）就必须走 Service。

---

### 4.5 后端：MyBatis-Plus 配置（分页插件 + 自动填充）

新建 [config/MyBatisPlusConfig.java](backend/src/main/java/com/seckill/config/MyBatisPlusConfig.java)：

```java
@Configuration
@MapperScan("com.seckill.mapper")   // 扫描 Mapper 接口（也可以放在启动类上）
public class MyBatisPlusConfig {

    /** 分页插件：不配这个，selectPage() 查不出总数和总页数 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /** 自动填充插件：Entity 里标注了 @TableField(fill=...) 的字段，插入/更新时自动填值 */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

**为什么需要自动填充？**

每个 Entity 都有 `createTime` 和 `updateTime`，如果每次 insert/update 都要手动 `user.setCreateTime(LocalDateTime.now())`，代码会很啰嗦。自动填充让 MyBatis-Plus 在执行 INSERT 时自动帮你填上当前时间。

---

### 4.6 后端验证

```bash
cd backend && ./mvnw compile
```

看到 `BUILD SUCCESS` 即可。如果报错找不到 `PaginationInnerInterceptor` 或 `MetaObjectHandler`，说明 pom.xml 里缺少 MyBatis-Plus 依赖。

---

### 4.7 前端：首页布局 —— 公共头部 + 路由出口

现在后端有 4 个接口了，前端需要多个页面：商品列表、秒杀专场、登录、注册。

首先需要一个**公共布局**，包含顶部导航栏，中间区域根据 URL 显示不同页面。

新建 [frontend/src/views/Layout.vue](frontend/src/views/Layout.vue)：

```html
<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <h1 @click="$router.push('/')">秒杀平台</h1>
      </div>
      <div class="header-right">
        <template v-if="userStore.token">
          <!-- 已登录：显示用户名和退出按钮 -->
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <!-- 未登录：显示登录和注册按钮 -->
          <el-button size="small" @click="$router.push('/login')">登录</el-button>
          <el-button size="small" type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <!-- 页面主体：根据 URL 显示对应的页面组件 -->
    <el-main>
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user.js'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()      // 清除 Pinia 里的 token 和 username
  router.push('/login')   // 跳回登录页
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  background: #409eff; color: white; padding: 0 24px; height: 60px;
}
.header-left h1 { cursor: pointer; font-size: 20px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.username { color: white; }
</style>
```

---

### 4.8 前端：更新路由 —— 多层嵌套

修改 [frontend/src/router/index.js](frontend/src/router/index.js)：

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,          // 所有页面都套在 Layout 里（有公共导航栏）
      redirect: '/products',      // 访问 / 自动跳转到商品列表
      children: [                 // children = 嵌套路由，内容显示在 Layout 的 <router-view> 里
        {
          path: 'products',
          name: 'ProductList',
          component: () => import('../views/ProductList.vue')
        },
        {
          path: 'seckill',
          name: 'SeckillList',
          component: () => import('../views/SeckillList.vue')
        },
        {
          path: 'seckill/:seckillProductId',         // :seckillProductId 是动态参数
          name: 'SeckillDetail',
          component: () => import('../views/SeckillDetail.vue')
        }
      ]
    },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/Register.vue') }
  ]
})

// 路由守卫：未登录 → 跳登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (token) {
    if (to.path === '/login' || to.path === '/register') next('/products')
    else next()
  } else {
    if (to.path === '/login' || to.path === '/register') next()
    else next('/login')
  }
})

export default router
```

**新概念：嵌套路由**

```
/                    ← Layout.vue（导航栏 + 白底）
  /products          ← Layout 里面的 <router-view> 显示 ProductList.vue
  /seckill           ← Layout 里面的 <router-view> 显示 SeckillList.vue
  /seckill/5         ← Layout 里面的 <router-view> 显示 SeckillDetail.vue
/login               ← 独立页面，不套 Layout
/register            ← 独立页面，不套 Layout
```

---

### 4.9 前端：商品列表页

新建 [frontend/src/views/ProductList.vue](frontend/src/views/ProductList.vue)：

```html
<template>
  <div class="product-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部商品" name="products" />
      <el-tab-pane label="秒杀专场" name="seckill" />
    </el-tabs>

    <!-- 商品卡片网格 -->
    <div class="product-grid" v-loading="loading">
      <el-card v-for="item in productList" :key="item.id" class="product-card" shadow="hover">
        <img :src="item.imageUrl || 'https://via.placeholder.com/200'" class="product-img" />
        <div class="product-info">
          <h3>{{ item.name }}</h3>
          <p class="price">¥{{ item.originalPrice }}</p>
        </div>
      </el-card>
    </div>

    <!-- 分页器 -->
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      @current-change="fetchProducts"
      layout="prev, pager, next"
      background
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const activeTab = ref('products')
const loading = ref(false)
const productList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// onMounted：Vue 3 生命周期钩子，组件挂载到页面上后自动执行（相当于 window.onload）
onMounted(() => fetchProducts())

const fetchProducts = async () => {
  loading.value = true
  try {
    // 根据当前 Tab 调用不同接口
    const url = activeTab.value === 'products' ? '/products' : '/products/seckill'
    const res = await request.get(url, { params: { page: page.value, size: size.value } })
    productList.value = res.data.records  // Page 对象：records=数据列表, total=总数
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1  // 切换 Tab 时重置到第1页
  fetchProducts()
}
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); /* 自适应列数 */
  gap: 16px;
  margin: 20px 0;
}
.product-card { cursor: pointer; }
.product-img { width: 100%; height: 160px; object-fit: cover; }
.product-info h3 { font-size: 16px; margin: 8px 0; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
</style>
```

**Vue 3 新概念**：

| 代码 | 含义 |
|------|------|
| `onMounted(() => {...})` | 页面加载完成后自动执行的回调 |
| `v-loading="loading"` | Element Plus 指令：加载中时显示转圈遮罩 |
| `v-model:current-page="page"` | 双向绑定分页器的当前页（Vue 3 的 v-model 语法糖） |
| `v-for="item in list"` | 循环渲染，每个 item 生成一张卡片 |
| `:key="item.id"` | 给每个循环项一个唯一标识，Vue 用它来高效更新 DOM |
| `grid-template-columns: repeat(auto-fill, minmax(220px, 1fr))` | CSS Grid 自适应：每列最小 220px，自动填充 |

---

### 4.10 前端：API 层补充

修改 [frontend/src/api/auth.js](frontend/src/api/auth.js)，增加商品相关：

```javascript
// 在原有基础上新增：
export function getProducts(params) {
  return request.get('/products', { params })        // GET /api/products?page=1&size=10
}
export function getSeckillProducts(params) {
  return request.get('/products/seckill', { params }) // GET /api/products/seckill?page=1&size=10
}
export function getSeckillDetail(seckillProductId) {
  return request.get(`/products/seckill/${seckillProductId}`) // GET /api/products/seckill/5
}
```

---

### 4.11 验证商品模块

1. **插入测试数据**（用命令行或 MySQL 插件）：
   ```sql
   INSERT INTO product (name, original_price, status, image_url)
   VALUES ('iPhone 15 Pro', 8999.00, 1, 'https://via.placeholder.com/200');
   
   INSERT INTO seckill_product (product_id, seckill_price, stock, start_time, end_time, status)
   VALUES (1, 5999.00, 100, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);
   ```

2. 启动后端和前端，打开 `http://localhost:3000`，应该看到商品卡片列表。

3. 切换到"秒杀专场" Tab，应该看到秒杀活动。

---

### 4.12 模块 3 小结

后端新增：
```
ProductService.java              ← 商品业务接口
ProductServiceImpl.java          ← 商品分页查询
SeckillProductService.java       ← 秒杀活动业务接口
SeckillProductServiceImpl.java   ← 秒杀活动查询 + 关联商品信息填充
ProductController.java           ← 公开商品 API
AdminProductController.java      ← 后台管理 API
MyBatisPlusConfig.java           ← 分页插件 + 自动填充配置
```

前端新增/修改：
```
Layout.vue                       ← 公共顶部导航栏布局
ProductList.vue                  ← 商品列表/秒杀列表页
router/index.js                  ← 嵌套路由（更新）
api/auth.js                      ← 新增商品 API 函数（修改）
```

---

## 第五章：模块 4 —— 秒杀核心（前后端联动）

> **目标**：实现秒杀的核心功能 —— 用户点击"立即抢购"后，后端通过 Redis Lua 脚本原子性地判断库存、防重复购买、扣减库存，成功则发消息给 RabbitMQ 异步创建订单。
>
> **你将学到**：Lua 脚本的原子性原理、Redis 在秒杀中的应用、RabbitMQ 消息队列的异步削峰、前端倒计时和按钮状态机。

### 5.0 秒杀的核心挑战：100 个人抢 10 个库存

假设 iPhone 15 Pro 秒杀价 5999，只有 10 台库存。如果几千人同时点"抢购"，后端收到几千个并发请求。

**如果不用 Redis**：几千个请求同时去 MySQL `UPDATE seckill_product SET stock=stock-1 WHERE id=? AND stock>0`，MySQL 的行锁会让所有请求排队，数据库直接被打挂。

**用 Redis**：库存数据存在 Redis 内存里，Lua 脚本一次性执行完"查库存→判断→扣减→标记用户"，整个过程是**原子**的（Redis 单线程执行，中间不会被其他请求打断），耗时 < 1ms。

整个秒杀流程：
```
用户点击"抢购"
    │
    ▼
前端倒计时结束，按钮亮起 → 用户滑动验证码 → 后端验证 captcha token
    │
    ▼
SeckillController.execute() — 从 request 中取 userId
    │
    ▼
SeckillService.executeSeckill() — 执行 Redis Lua 脚本
    │
    ├── 库存不足? → 返回 "已售罄"
    ├── 重复购买? → 返回 "已抢过"
    └── 成功! → Redis 扣库存 + 发送消息到 RabbitMQ → 返回 "排队中"
                                                         │
                                                         ▼
                                              RabbitMQ 消费者（异步）
                                                         │
                                                         ▼
                                              创建订单写入 MySQL
                                                         │
                                                         ▼
                                              前端每500ms轮询拿到结果
```

---

### 5.1 后端：Redis 配置

新建 [config/RedisConfig.java](backend/src/main/java/com/seckill/config/RedisConfig.java)：

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // Key 用 String 序列化
        template.setKeySerializer(RedisSerializer.string());
        // Value 用 JSON 序列化（方便查看）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // Hash 的 key 也用 String
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**为什么需要配置 RedisTemplate？** 默认的 JDK 序列化会把对象变成二进制乱码，在 Redis 里用 `KEYS *` 查看时完全看不懂。换成 JSON 序列化后，数据在 Redis 里是人类可读的 JSON。

---

### 5.2 后端：Lua 脚本 —— 秒杀的核心逻辑

新建 [resources/lua/seckill.lua](backend/src/main/resources/lua/seckill.lua)：

```lua
-- KEYS[1] = seckill:stock:{seckillProductId}  — 库存 key
-- KEYS[2] = seckill:users:{seckillProductId}  — 已购买用户集合 key
-- ARGV[1] = userId                             — 当前用户 ID

local stock = redis.call('GET', KEYS[1])       -- 1. 读取当前库存
if not stock or tonumber(stock) <= 0 then       -- 2. 库存不存在或已归零
    return -1                                   -- 返回 -1 表示 "已售罄"
end

local exists = redis.call('SISMEMBER', KEYS[2], ARGV[1])  -- 3. 检查用户是否已买过
if exists == 1 then
    return -2                                   -- 返回 -2 表示 "重复购买"
end

redis.call('DECR', KEYS[1])                    -- 4. 库存减一（原子操作）
redis.call('SADD', KEYS[2], ARGV[1])           -- 5. 把用户 ID 加入已购集合
return 1                                        -- 返回 1 表示 "抢购成功"
```

**为什么用 Lua？** 如果不写 Lua，后端需要分 4 次调用 Redis（GET → 判断 → DECR → SADD），这 4 步中间可能有其他请求插入，导致"库存剩 1，两个人同时通过判断，都执行 DECR，库存变成 -1"。Lua 脚本在 Redis 里一次性执行完 4 步，中间不会被打断。

**返回值约定**：
| 返回值 | 含义 |
|--------|------|
| 1 | 抢购成功 |
| -1 | 库存不足 |
| -2 | 已购买过（重复） |

---

### 5.3 后端：Lua 脚本加载配置

Lua 脚本通过 `RedisScript` 加载，Spring 在启动时把脚本内容读取到内存，执行时用 `EVALSHA`（通过 SHA1 哈希调用，不需要每次传脚本文本）。

新建 [config/LuaScriptConfig.java](backend/src/main/java/com/seckill/config/LuaScriptConfig.java)：

```java
@Configuration
public class LuaScriptConfig {

    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua"));  // 读取 resources/lua/seckill.lua
        script.setResultType(Long.class);  // 告诉 Spring 脚本返回的是数字
        return script;
    }
}
```

---

### 5.4 后端：RabbitMQ 配置

新建 [config/RabbitMQConfig.java](backend/src/main/java/com/seckill/config/RabbitMQConfig.java)：

```java
@Configuration
public class RabbitMQConfig {

    // ===== 定义交换机、队列、路由键的常量 =====
    public static final String EXCHANGE_NAME = "seckill.exchange";
    public static final String QUEUE_NAME = "seckill.order.queue";
    public static final String ROUTING_KEY = "seckill.order";

    /** 创建交换机（Topic 类型） */
    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false); // durable=true, autoDelete=false
    }

    /** 创建队列（持久化） */
    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    /** 把队列绑定到交换机 */
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(ROUTING_KEY);
    }

    /** JSON 消息转换器：Java 对象 → JSON → 发送到 RabbitMQ */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

**三个关键概念**：

| 概念 | 类比 | 本项目中的使用 |
|------|------|---------------|
| **Exchange（交换机）** | 邮局分拣中心 | 接收秒杀成功消息，按 routing key 分发 |
| **Queue（队列）** | 每个消费者的信箱 | 订单创建任务在这里排队 |
| **Routing Key（路由键）** | 邮政编码 | `seckill.order` 表示这是下单任务 |

---

### 5.5 后端：SeckillService —— 执行秒杀

新建 [service/SeckillService.java](backend/src/main/java/com/seckill/service/SeckillService.java)：

```java
package com.seckill.service;

public interface SeckillService {
    /** 执行秒杀，返回结果码：1=成功, -1=售罄, -2=重复 */
    Long executeSeckill(Long seckillProductId, Long userId);
}
```

新建 [service/impl/SeckillServiceImpl.java](backend/src/main/java/com/seckill/service/impl/SeckillServiceImpl.java)：

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillScript;
    private final RabbitTemplate rabbitTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USERS_KEY_PREFIX = "seckill:users:";

    @Override
    public Long executeSeckill(Long seckillProductId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + seckillProductId;
        String usersKey = USERS_KEY_PREFIX + seckillProductId;

        // execute(脚本, KEYS列表, ARGV列表...)
        // Redis 执行 Lua 脚本，整个过程原子性
        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(stockKey, usersKey),  // KEYS
                String.valueOf(userId)        // ARGV[1]
        );

        if (result != null && result == 1) {
            // 秒杀成功 → 发消息到 RabbitMQ（异步创建订单）
            SeckillMessage message = new SeckillMessage(seckillProductId, userId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    message
            );
            log.info("Seckill success: userId={}, seckillProductId={}", userId, seckillProductId);
        }
        return result;
    }
}
```

**关键细节**：
- `stringRedisTemplate.execute(script, keys, args)` — 把 Lua 脚本发给 Redis 执行，返回脚本的 return 值
- `rabbitTemplate.convertAndSend(exchange, routingKey, message)` — 发消息到 RabbitMQ，不阻塞当前请求
- 秒杀结果在 Redis 层就返回了（< 5ms），用户不需要等订单创建完成

---

### 5.6 后端：RabbitMQ 消费者 —— 异步创建订单

新建 [consumer/SeckillConsumer.java](backend/src/main/java/com/seckill/consumer/SeckillConsumer.java)：

```java
@Slf4j
@Component  // 注意：消费者用 @Component（不是 @Service），它是一个消息监听器
@RequiredArgsConstructor
public class SeckillConsumer {

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final RabbitTemplate rabbitTemplate;

    // @RabbitListener：声明这个方法监听哪个队列
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleSeckillOrder(Message message, Channel channel) {
        SeckillMessage msg = (SeckillMessage) rabbitTemplate.getMessageConverter()
                .fromMessage(message);   // JSON → Java 对象

        try {
            // 第1步：查秒杀商品信息
            SeckillProduct sp = seckillProductMapper.selectById(msg.getSeckillProductId());
            if (sp == null) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 第2步：乐观锁扣库存（三层防超卖之第三层）
            // UPDATE seckill_product SET stock = stock - 1, version = version + 1
            // WHERE id = ? AND version = ? AND stock > 0
            LambdaUpdateWrapper<SeckillProduct> wrapper = new LambdaUpdateWrapper<SeckillProduct>()
                    .eq(SeckillProduct::getId, sp.getId())
                    .eq(SeckillProduct::getVersion, sp.getVersion())  // 乐观锁：检查版本号
                    .gt(SeckillProduct::getStock, 0)                  // 库存大于 0
                    .set(SeckillProduct::getStock, sp.getStock() - 1) // stock = stock - 1
                    .set(SeckillProduct::getVersion, sp.getVersion() + 1); // version + 1

            int updated = seckillProductMapper.update(null, wrapper);
            if (updated == 0) {
                log.warn("Optimistic lock failed or stock exhausted for {}", msg.getSeckillProductId());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 第3步：创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo(msg.getUserId()));
            order.setUserId(msg.getUserId());
            order.setSeckillProductId(msg.getSeckillProductId());
            order.setProductId(sp.getProductId());
            order.setSeckillPrice(sp.getSeckillPrice());
            order.setStatus(0);  // 0=未支付

            try {
                orderMapper.insert(order);  // 插入成功
            } catch (DuplicateKeyException e) {
                // 唯一索引 uk_user_seckill 冲突：用户已买过（三层防超卖之第二层）
                log.warn("Duplicate order prevented: userId={}, spId={}",
                        msg.getUserId(), msg.getSeckillProductId());
            }

            // 第4步：手动确认消息（ACK）
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Order creation failed: {}", e.getMessage());
            // 异常时 NACK 并重新入队（让其他消费者重试）
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    /** 生成订单编号：ORD + 年月日 + 时分秒 + 用户ID后4位 */
    private String generateOrderNo(Long userId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + now + String.format("%04d", userId % 10000);
    }
}
```

**消费者工作流程**：
```
RabbitMQ 队列 → @RabbitListener 收到消息 → JSON → SeckillMessage 对象
    → 检查秒杀商品是否存在
    → 乐观锁扣库存（UPDATE WHERE version=? AND stock>0）
        ├── 失败 → ACK（丢弃消息）
        └── 成功 → 创建订单
                ├── 唯一索引冲突（已买过）→ 忽略 → ACK
                └── 成功 → ACK
```

**手动 ACK 的意义**：`acknowledge-mode: manual` 表示消费者处理成功后手动确认。如果消费者挂了但没 ACK，RabbitMQ 会把消息重新发给其他消费者。这保证了 **at-least-once** 语义。

---

### 5.7 后端：SeckillMessage —— 消息体

新建 [dto/SeckillMessage.java](backend/src/main/java/com/seckill/dto/SeckillMessage.java)：

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    private Long seckillProductId;
    private Long userId;
}
```

---

### 5.8 后端：SeckillController —— 秒杀接口

新建 [controller/SeckillController.java](backend/src/main/java/com/seckill/controller/SeckillController.java)：

```java
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Slf4j
public class SeckillController {

    private final SeckillService seckillService;

    /** POST /api/seckill/{seckillProductId}/execute — 执行秒杀 */
    @PostMapping("/{seckillProductId}/execute")
    public Result<String> execute(@PathVariable Long seckillProductId, HttpServletRequest request) {
        // 从 JWT 拦截器设置的 request 属性中取出 userId
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        Long result = seckillService.executeSeckill(seckillProductId, userId);

        if (result == 1) {
            return Result.ok("抢购成功，订单处理中");  // 异步处理，先返回成功
        } else if (result == -1) {
            return Result.fail("很遗憾，已售罄");
        } else if (result == -2) {
            return Result.fail("您已经抢过了");
        } else {
            return Result.fail("秒杀失败");
        }
    }

    /** GET /api/seckill/{seckillProductId}/result?userId=xxx — 轮询秒杀结果 */
    @GetMapping("/{seckillProductId}/result")
    public Result<?> getResult(@PathVariable Long seckillProductId,
                               @RequestParam Long userId) {
        // 查数据库该用户是否有这个秒杀活动的订单
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getSeckillProductId, seckillProductId));
        if (order != null) {
            return Result.ok(order);  // 订单已生成 = 秒杀成功
        }
        return Result.ok(null);  // 还没生成 = 还在排队
    }
}
```

---

### 5.9 前端：秒杀详情页 —— 倒计时 + 抢购按钮

新建 [frontend/src/views/SeckillDetail.vue](frontend/src/views/SeckillDetail.vue)：

```html
<template>
  <div class="seckill-detail" v-loading="loading">
    <el-row :gutter="24">
      <!-- 商品图片 -->
      <el-col :span="8">
        <img :src="detail.imageUrl || 'https://via.placeholder.com/400'" class="detail-img" />
      </el-col>
      <!-- 商品信息 + 抢购区域 -->
      <el-col :span="16">
        <h1>{{ detail.productName }}</h1>
        <div class="price-area">
          <span class="seckill-price">¥{{ detail.seckillPrice }}</span>
          <span class="original-price">¥{{ detail.originalPrice }}</span>
        </div>
        <div class="stock-info">
          剩余库存：<span class="stock-num">{{ detail.stock }}</span>
        </div>

        <!-- 倒计时 -->
        <div class="countdown" v-if="countdownText">
          距离{{ countdownLabel }}还有：<span class="time">{{ countdownText }}</span>
        </div>

        <!-- 抢购按钮 -->
        <el-button
          type="danger"
          size="large"
          :disabled="btnDisabled"
          @click="handleSeckill"
          :loading="seckilling"
        >
          {{ btnText }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'       // 获取 URL 参数
import { ElMessage } from 'element-plus'
import request from '../api/request.js'

const route = useRoute()
const seckillProductId = route.params.seckillProductId  // 从 URL /seckill/5 中取 5

const loading = ref(false)
const seckilling = ref(false)
const detail = ref({})
const now = ref(Date.now())        // 当前时间（用于计算倒计时）
let timer = null

// ===== 在页面加载时：查秒杀详情 + 启动倒计时 =====
onMounted(async () => {
  loading.value = true
  const res = await request.get(`/products/seckill/${seckillProductId}`)
  detail.value = res.data
  loading.value = false
  // 每秒更新当前时间，触发倒计时重新计算
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

onUnmounted(() => clearInterval(timer))  // 离开页面时清除定时器

// ===== 计算倒计时文本和按钮状态 =====
const countdownInfo = computed(() => {
  const start = new Date(detail.value.startTime).getTime()
  const end = new Date(detail.value.endTime).getTime()
  if (now.value < start) {
    // 活动还没开始
    const diff = Math.floor((start - now.value) / 1000)
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    return { label: '开始', text: `${h}时${m}分${s}秒`, state: 'before' }
  } else if (now.value >= start && now.value <= end) {
    // 活动进行中
    const diff = Math.floor((end - now.value) / 1000)
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    return { label: '结束', text: `${h}时${m}分${s}秒`, state: 'going' }
  } else {
    // 活动已结束
    return { label: '', text: '', state: 'ended' }
  }
})

const countdownLabel = computed(() => countdownInfo.value.label)
const countdownText = computed(() => countdownInfo.value.text)

const btnDisabled = computed(() => countdownInfo.value.state !== 'going' || seckilling.value)

const btnText = computed(() => {
  if (countdownInfo.value.state === 'before') return '尚未开始'
  if (countdownInfo.value.state === 'ended') return '已结束'
  return seckilling.value ? '抢购中...' : '立即抢购'
})

// ===== 点击抢购 =====
const handleSeckill = async () => {
  seckilling.value = true
  try {
    const res = await request.post(`/seckill/${seckillProductId}/execute`)
    if (res.code === 200) {
      ElMessage.success('抢购成功！订单生成中...')
      pollResult()  // 开始轮询
    }
  } catch (error) {
    // 错误已在拦截器中提示
  } finally {
    seckilling.value = false
  }
}

// ===== 轮询秒杀结果（每 500ms 查一次） =====
const pollResult = () => {
  const userId = JSON.parse(atob(localStorage.getItem('token').split('.')[1])).sub
  const poll = setInterval(async () => {
    const res = await request.get(`/seckill/${seckillProductId}/result`, {
      params: { userId }
    })
    if (res.data && res.data.id) {
      clearInterval(poll)
      ElMessage.success('订单已生成！')
      // 跳转到订单页（后面实现）
    }
  }, 500)
  // 最多轮询 30 秒
  setTimeout(() => clearInterval(poll), 30000)
}
</script>

<style scoped>
.seckill-detail { padding: 24px; max-width: 1000px; margin: 0 auto; }
.detail-img { width: 100%; border-radius: 8px; }
.seckill-price { color: #f56c6c; font-size: 32px; font-weight: bold; margin-right: 12px; }
.original-price { color: #999; text-decoration: line-through; font-size: 18px; }
.stock-info { margin: 12px 0; font-size: 16px; }
.stock-num { color: #f56c6c; font-weight: bold; }
.countdown { margin: 16px 0; font-size: 16px; }
.countdown .time { color: #f56c6c; font-size: 24px; font-weight: bold; }
</style>
```

---

### 5.10 前端：秒杀列表页

新建 [frontend/src/views/SeckillList.vue](frontend/src/views/SeckillList.vue)：

```html
<template>
  <div class="seckill-list" v-loading="loading">
    <el-empty v-if="!loading && list.length === 0" description="暂无秒杀活动" />
    <div class="seckill-grid">
      <el-card v-for="item in list" :key="item.id" class="seckill-card" shadow="hover"
               @click="$router.push(`/seckill/${item.id}`)">
        <img :src="item.imageUrl || 'https://via.placeholder.com/200'" />
        <div class="seckill-info">
          <h3>{{ item.productName }}</h3>
          <p class="seckill-price">¥{{ item.seckillPrice }}
            <span class="original-price">¥{{ item.originalPrice }}</span>
          </p>
          <el-tag type="danger" size="small">限量 {{ item.stock }} 件</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const loading = ref(false)
const list = ref([])

onMounted(async () => {
  loading.value = true
  const res = await request.get('/products/seckill', { params: { page: 1, size: 20 } })
  list.value = res.data.records
  loading.value = false
})
</script>

<style scoped>
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 20px;
}
.seckill-card { cursor: pointer; }
.seckill-card img { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.seckill-price { color: #f56c6c; font-size: 20px; font-weight: bold; margin: 8px 0; }
.original-price { color: #999; text-decoration: line-through; font-size: 14px; margin-left: 8px; }
</style>
```

---

### 5.11 模块 4 小结

后端新增：
```
lua/seckill.lua                  ← Redis Lua 原子扣库存脚本
RedisConfig.java                 ← Redis 序列化配置
LuaScriptConfig.java             ← 加载 Lua 脚本为 Bean
RabbitMQConfig.java              ← 交换机/队列/绑定 配置
SeckillMessage.java              ← 消息体 DTO
SeckillService.java              ← 秒杀业务接口
SeckillServiceImpl.java          ← 执行 Lua 脚本 + 发送 RabbitMQ 消息
SeckillConsumer.java             ← RabbitMQ 消费者：异步创建订单
SeckillController.java           ← 秒杀接口 + 结果轮询
```

前端新增：
```
SeckillDetail.vue                ← 秒杀详情页（倒计时 + 抢购按钮 + 轮询）
SeckillList.vue                  ← 秒杀活动列表页
```

---

## 第六章：模块 5 —— 限流与防护

> **目标**：用 Redis 令牌桶算法做接口限流，防止恶意刷接口和机器人抢购。
>
> **你将学到**：令牌桶算法原理、拦截器链（多个拦截器如何协同工作）、自定义注解。

### 6.0 为什么需要限流？

秒杀开始瞬间，可能有几千人同时点"抢购"。SecKillController 每秒收到几千个请求。虽然 Redis Lua 很快，但后端 Tomcat 线程池有限（默认 200 线程），如果 200 个线程都在执行 Lua 脚本，新来的请求只能排队等。

**限流的作用**：在请求到达 Controller 之前就拦截掉一部分，只让合理的流量进入秒杀流程。

**令牌桶算法**：
```
每 100ms 生成一个令牌放进桶里（桶最多装 100 个令牌）
    ↓
请求来了 → 从桶里拿一个令牌 → 拿到 → 放行
                          → 拿不到 → 返回 "系统繁忙，请稍后"
```

桶容量 100 = 允许瞬间爆发 100 个请求（应对秒杀刚开始的流量洪峰）。令牌生成速度 = 每秒 10 个 = 持续 QPS 限制在 10。这样既给了"爆发空间"，又限制了"持续压力"。

---

### 6.1 后端：Redis 令牌桶 Lua 脚本

新建 [resources/lua/rate_limit.lua](backend/src/main/resources/lua/rate_limit.lua)：

```lua
-- KEYS[1] = rate_limit:{key}      — 限流 key
-- ARGV[1] = capacity              — 令牌桶最大容量
-- ARGV[2] = rate                  — 令牌生成速率（个/秒）
-- ARGV[3] = now                   — 当前时间戳（毫秒）

local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Redis 存两个值：tokens（当前令牌数） 和 last_time（上次填充时间）
local data = redis.call('HMGET', KEYS[1], 'tokens', 'last_time')
local tokens = tonumber(data[1])
local last_time = tonumber(data[2])

if tokens == nil then
    tokens = capacity         -- 第一次访问，给满桶
    last_time = now
end

-- 计算从上次到现在的间隔，生成新令牌：间隔秒数 × 速率
local delta = math.max(0, now - last_time)
local new_tokens = math.floor(delta / 1000 * rate)
tokens = math.min(capacity, tokens + new_tokens)  -- 不能超过容量
last_time = now

-- 消耗一个令牌
if tokens > 0 then
    tokens = tokens - 1
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_time', last_time)
    redis.call('EXPIRE', KEYS[1], 300)  -- 5 分钟不活跃自动过期
    return 1   -- 通过
else
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_time', last_time)
    return 0   -- 限流
end
```

---

### 6.2 后端：自定义注解 —— @RateLimited

我们希望注解一个方法就能限流，例如 `@RateLimited(key = "seckill", capacity = 100, rate = 10)`。

新建 [common/RateLimited.java](backend/src/main/java/com/seckill/common/RateLimited.java)：

```java
@Target(ElementType.METHOD)  // 只能用在方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留（反射能读到）
public @interface RateLimited {
    String key() default "default";       // 限流标识
    int capacity() default 100;           // 令牌桶容量
    int rate() default 10;                // 令牌生成速率（个/秒）
}
```

---

### 6.3 后端：RateLimitInterceptor —— 限流拦截器

新建 [interceptor/RateLimitInterceptor.java](backend/src/main/java/com/seckill/interceptor/RateLimitInterceptor.java)：

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 检查 handler 是否是被 @RateLimited 标注的方法
        if (handler instanceof HandlerMethod hm) {
            RateLimited annotation = hm.getMethodAnnotation(RateLimited.class);
            if (annotation != null) {
                String key = "rate_limit:" + annotation.key();
                Long result = stringRedisTemplate.execute(
                        rateLimitScript,
                        List.of(key),
                        String.valueOf(annotation.capacity()),
                        String.valueOf(annotation.rate()),
                        String.valueOf(System.currentTimeMillis())
                );
                if (result == null || result == 0) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(429);  // HTTP 429 = Too Many Requests
                    response.getWriter().write("{\"code\":429,\"message\":\"系统繁忙，请稍后再试\"}");
                    return false;
                }
            }
        }
        return true;
    }
}
```

然后在 [LuaScriptConfig.java](backend/src/main/java/com/seckill/config/LuaScriptConfig.java) 中补充：

```java
@Bean
public DefaultRedisScript<Long> rateLimitScript() {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setLocation(new ClassPathResource("lua/rate_limit.lua"));
    script.setResultType(Long.class);
    return script;
}
```

在 [WebConfig.java](backend/src/main/java/com/seckill/config/WebConfig.java) 中注册（**注意顺序**：限流拦截器在 JWT 之后）：

```java
private final RateLimitInterceptor rateLimitInterceptor;

@Override
public void addInterceptors(InterceptorRegistry registry) {
    // 先 JWT 验证身份，再限流检查
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/login", "/api/auth/register",
                    "/api/products", "/api/products/*/seckill");

    // 限流拦截器只拦截秒杀接口
    registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/seckill/**");
}
```

**拦截器链的执行顺序**：
```
请求 → JwtInterceptor（先验证登录）→ RateLimitInterceptor（再限流）→ Controller
```

---

## 第七章：模块 6 —— 订单与收尾

> **目标**：实现订单查询功能，用户可以看到自己的秒杀订单。最后配置 Nginx 反向代理 + JMeter 压测验证系统性能。
>
> **你将学到**：订单管理、Nginx 配置、JMeter 压测基本操作。

### 7.1 后端：OrderService

新建 [service/OrderService.java](backend/src/main/java/com/seckill/service/OrderService.java)：

```java
package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Order;

public interface OrderService {
    /** 分页查询用户的订单列表 */
    Page<Order> listUserOrders(Long userId, int page, int size);
    /** 查单个订单详情 */
    Order getOrderById(Long orderId);
}
```

新建 [service/impl/OrderServiceImpl.java](backend/src/main/java/com/seckill/service/impl/OrderServiceImpl.java)：

```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<Order> listUserOrders(Long userId, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        // 填充商品名称
        for (Order order : result.getRecords()) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        }
        return result;
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        }
        return order;
    }
}
```

---

### 7.2 后端：OrderController

新建 [controller/OrderController.java](backend/src/main/java/com/seckill/controller/OrderController.java)：

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** GET /api/orders — 我的订单列表 */
    @GetMapping
    public Result<Page<Order>> list(HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.listUserOrders(userId, page, size));
    }

    /** GET /api/orders/{id} — 订单详情 */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) return Result.fail("订单不存在");
        return Result.ok(order);
    }
}
```

---

### 7.3 前端：订单列表页

新建 [frontend/src/views/OrderList.vue](frontend/src/views/OrderList.vue)：

```html
<template>
  <div class="order-list" v-loading="loading">
    <h2>我的订单</h2>
    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单编号" width="220" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column label="秒杀价格" width="120">
        <template #default="{ row }"> ¥{{ row.seckillPrice }} </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }"> {{ row.createTime }} </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :total="total" :page-size="size"
      @current-change="fetchOrders"
      layout="prev, pager, next" background
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusMap = {
  0: { text: '未支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已取消', type: 'info' },
  3: { text: '已退款', type: 'danger' }
}

onMounted(() => fetchOrders())

const fetchOrders = async () => {
  loading.value = true
  const res = await request.get('/orders', { params: { page: page.value, size: size.value } })
  orders.value = res.data.records
  total.value = res.data.total
  loading.value = false
}
</script>

<style scoped>
.order-list { padding: 20px; }
</style>
```

在路由中补充订单页路由：
```javascript
{
  path: 'orders',
  name: 'OrderList',
  component: () => import('../views/OrderList.vue')
}
```

---

### 7.4 Nginx 配置（生产环境）

```nginx
upstream backend {
    server 127.0.0.1:8080;
    # 如果有多个后端实例：
    # server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name seckill.local;

    # 前端静态文件
    location / {
        root /home/ydmy/seckill-platform/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;  # Vue Router history 模式需要
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

### 7.5 JMeter 压测指南

1. **下载 JMeter**：`sudo apt install jmeter`
2. **创建测试计划**：
   - Thread Group：1000 线程，Ramp-up 1 秒（1 秒内启动 1000 个线程）
   - HTTP Request：POST `http://localhost:8080/api/seckill/1/execute`
   - HTTP Header Manager：添加 `Authorization: Bearer <token>`
3. **运行**：`jmeter -n -t seckill-test.jmx -l result.jtl`
4. **看结果**：聚合报告中关注 Average（平均响应时间）、Throughput（QPS）、Error%（错误率）

---

## 第八章：学习路线总结

学完这七个模块，你应该能回答：

1. **一个 HTTP 请求从浏览器到数据库经历了什么？**（见 0.4 节）
2. **JWT 登录验证的完整流程？**（见 3.6 节）
3. **秒杀为什么快？** Redis Lua 原子化 < 5ms（见 5.2 节）
4. **如何防止超卖？** 三层防护：Redis Lua + DB 唯一索引 + DB 乐观锁（见 5.6 节）
5. **消息队列的作用？** 异步削峰，秒杀结果快速返回，订单创建慢慢来（见 5.6 节）
6. **限流怎么实现？** Redis 令牌桶 + 自定义注解 + 拦截器（见第六章）

**下一步你可以**：
- 加上滑块验证码（滑动拼图）
- 用 Redis Cluster 做分片
- 把后端部署到云服务器
- 加 Docker Compose 一键启动

---

## 附录：面试 20 问

> 面试前花 30 分钟过一遍这些问题。

### 基础篇

**Q1: 这个项目的核心难点是什么？**
> 高并发下的库存一致性。我用"Redis Lua 原子扣减 + DB 乐观锁 + DB 唯一索引"三层防超卖机制，秒杀响应在 Redis 层就返回（< 5ms），用户不需要等 DB 写入。

**Q2: 为什么不能只靠 Redis 扣库存？**
> Redis 是内存数据库，极端情况下（断电）可能丢失数据。MySQL 的 ACID 事务是最可靠的数据保障。Redis 承担"拦截流量"角色，MySQL 是最终数据的"source of truth"。

**Q3: 秒杀 QPS 能到多少？瓶颈在哪？**
> 单机约 5000 QPS（Redis 层 < 5ms）。瓶颈：Tomcat 线程数（默认 200）、GC 压力、网络带宽。突破方案：Nginx + OpenResty Lua 限流、Redis Cluster、后端水平扩展。

**Q4: Lua 脚本为什么是原子的？**
> Redis 是单线程执行命令的。Lua 脚本提交后，Redis 把它当成一条命令执行，中间不会插入其他命令。所以"读库存→判断→扣减→标记"这四步不会被打断。

**Q5: 如果 Redis 挂了呢？**
> Redis Sentinel 主从自动切换保证高可用。代码层面捕获 Redis 连接异常，降级为直接查数据库。同时前端显示"系统繁忙"。

**Q6: 为什么用 StringRedisTemplate 而不是 RedisTemplate？**
> 秒杀路径只传简单的字符串和数字。StringRedisTemplate 省去序列化开销，Lua 脚本的 KEYS 和 ARGV 都是 String 类型。

**Q7: 消息队列如果丢消息怎么办？**
> RabbitMQ 配合手动 ACK 实现 at-least-once 语义。消息和队列都声明 `durable=true`。代码层面通过 DB 唯一索引防重。

**Q8: 如何防止同一用户重复抢购？**
> 两层防护：Redis Lua 脚本里 `SISMEMBER` 检查已购集合（快速拦截）；MySQL `uk_user_seckill` 唯一索引（最终保障）。

**Q9: 为什么要分三层防超卖？**
> 每层职责不同，互相兜底：
> - Layer 1 (Redis Lua): 极速拦截，处理 99% 请求
> - Layer 2 (DB 唯一索引): 防止同一用户重复购买
> - Layer 3 (DB 乐观锁): 防止库存扣到负数

**Q10: 乐观锁和悲观锁怎么选？**
> 秒杀场景用乐观锁。悲观锁让所有请求排队等行锁，QPS 极低。乐观锁放在异步消费者中，前面 Redis Lua 已经过滤了绝大部分并发，到达时冲突概率极低。

### 进阶篇

**Q11: 为什么使用 RabbitMQ 而不是 Kafka？**
> RabbitMQ 更适合业务场景的低延迟消息（毫秒级）。Kafka 适合大数据日志场景（高吞吐、允许秒级延迟）。秒杀订单需要"尽快创建"，RabbitMQ 更合适。而且 RabbitMQ 的手动 ACK 机制对"处理完才确认"的场景更友好。

**Q12: Redis 库存预热是什么？**
> 在秒杀开始前，提前把数据库里的 `stock` 值写入 Redis。秒杀过程中直接在 Redis 里扣减，结束后再同步回数据库。避免用户请求打到 MySQL。

**Q13: 你的项目能应对缓存穿透、击穿和雪崩吗？**
> - 穿透（查不存在的 key）：不存在的数据也缓存 null 值
> - 击穿（一个热点 key 过期）：互斥锁，同一时间只让一个线程去查 DB
> - 雪崩（大量 key 同时过期）：TTL 加随机值打散过期时间，不设同一过期时间

**Q14: 为什么要用 MyBatis-Plus 而不是 JPA？**
> LambdaQueryWrapper 编译期检查字段引用，不会拼错列名。完整 SQL 控制权在手（`SELECT ... FOR UPDATE`、复杂的 JOIN 场景），不像 JPA 框架自动生成 SQL 不可控。

**Q15: 依赖注入的好处是什么？**
> 解耦。Controller 只声明 `private final UserService userService`，不关心实现类是什么。测试时可以注入 Mock 实现，线上注入真实实现。对象创建由 Spring 管理，避免手动 `new`。

**Q16: JWT 和 Session 的区别？**
> - Session：用户信息存在服务端内存，需要查 Session 存储，多台服务器需要同步
> - JWT：用户信息存在 Token 里（客户端），无状态，任何服务器都能验证，天然支持水平扩展

**Q17: @Transactional 事务在秒杀中怎么用？**
> 秒杀路径上不推荐用大事务。Redis 扣库存不在事务内（Redis 没有 JDBC 事务）。异步消费者创建订单时，最重要的是乐观锁（`UPDATE WHERE version=?`），而不是 `@Transactional`。事务范围要尽量小。

**Q18: 你的项目怎么部署到生产环境？**
> - 前端 `npm run build` → Nginx 托管 `/dist`
> - 后端 `./mvnw package -DskipTests` → `java -jar seckill-platform-1.0.0.jar`
> - Nginx 反向代理 `/api/` 到 `localhost:8080`
> - Docker Compose 管理 MySQL + Redis + RabbitMQ
> - Jenkins / GitHub Actions 做 CI/CD

**Q19: 怎么统计秒杀 QPS？**
> 可以用 `@Aspect` 切面在每个 Controller 方法前后记录时间戳，统计 1 秒内的请求数。或者 Nginx 的 `$request_time` 日志分析。JMeter 压测时聚合报告直接能看到 QPS。

**Q20: 这个项目你学到了什么？**
> 1. Spring Boot 全栈开发能力（Entity/Mapper/Service/Controller 分层）
> 2. Redis 高性能缓存 + Lua 原子操作
> 3. RabbitMQ 异步削峰解耦
> 4. 高并发场景的防超卖三层设计
> 5. JWT 无状态认证
> 6. 系统设计：从"功能跑通"到"扛住流量"的思维转变

---

> **恭喜你看完了整个教程！** 现在打开你 IDE 里的源码文件，对照着 README 中的代码注释，一行一行地读。遇到不懂的随时问我。
