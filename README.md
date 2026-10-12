# 电子图书馆后端系统

基于Spring Boot、Spring Security、Java 17、MyBatis和PostgreSQL的电子图书馆后端服务平台。

## 项目概述

电子图书馆平台让用户能够浏览、借阅和阅读数字内容，例如电子书和期刊。系统提供完整的图书管理、用户管理和借阅管理功能，并集成Spring Security + JWT实现身份认证与权限控制。

## 主要功能

### 1. 书籍管理
- 浏览所有书籍（支持分页和分类）
- 搜索书籍（按标题、作者、描述等）
- 查看书籍详细信息
- 创建/更新/删除书籍
- 管理书籍副本（总数量、可用数量）

### 2. 借阅管理
- 借阅书籍
- 归还书籍
- 续借书籍
- 查看借阅记录
- 逾期罚款计算
- 书籍丢失报告

### 3. 用户管理
- 用户信息管理
- 借阅限制控制
- 权限管理（用户/管理员）

### 4. 认证与安全
- 基于Spring Security + JWT的身份认证
- 自定义`@LoginRequired`注解式登录验证
- BCrypt密码加密存储，不可逆
- 无状态Token认证，服务器重启不影响
- 支持`@PreAuthorize`角色权限控制

### 5. 系统功能
- 完整的异常处理机制
- 统一的日志记录
- API文档（Swagger）
- 数据验证
- 事务管理

## 技术栈

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security** (身份认证框架)
- **JWT (jjwt 0.12.5)** (令牌生成与校验)
- **BCrypt** (密码加密)
- **MyBatis 3.0.3**
- **PostgreSQL 14+**
- **Lombok**
- **MapStruct**
- **SpringDoc OpenAPI (Swagger)**
- **Logback**
- **JUnit 5** (测试)
- **H2 Database** (测试)

## 项目结构

```
src/main/java/com/elibrary/
├── ELibraryApplication.java          # 应用启动类
├── config/                           # 配置类
│   ├── MyBatisConfig.java
│   └── SwaggerConfig.java
├── controller/                       # 控制器层
│   ├── BookController.java
│   └── BorrowController.java
├── service/                          # 服务层
│   ├── BookService.java
│   ├── BorrowService.java
│   └── impl/
│       ├── BookServiceImpl.java
│       └── BorrowServiceImpl.java
├── repository/                       # 仓储层
│   ├── BookRepository.java
│   ├── BorrowRecordRepository.java
│   └── UserRepository.java
├── mapper/                           # MyBatis映射器
│   ├── BookMapper.java
│   └── BookMapper.xml
├── model/                           # 数据模型
│   ├── entity/                      # 实体类
│   │   ├── Book.java
│   │   ├── BorrowRecord.java
│   │   └── User.java
│   └── dto/                         # 数据传输对象
│       ├── BookDTO.java
│       ├── CreateBookDTO.java
│       ├── BookSearchDTO.java
│       └── BorrowRecordDTO.java
└── common/                          # 通用组件
    ├── aop/                         # 切面
    │   └── LogAspect.java
    ├── exception/                   # 异常处理
    │   ├── BusinessException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── response/                    # 响应封装
    │   └── ApiResponse.java
    └── log/                         # 日志配置
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Git

### 2. 数据库设置

```bash
# 创建数据库
createdb elibrary

# 导入数据库脚本
psql -d elibrary -f database/init.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/elibrary
    username: your_username
    password: your_password
```

### 4. 构建和运行

```bash
# 克隆项目
git clone <repository-url>
cd eLibaray

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package

# 运行应用
java -jar target/e-library-1.0.0.jar
```

### 5. 访问API文档

应用启动后，访问以下URL：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API文档: http://localhost:8080/api/api-docs

## API接口

### 书籍相关接口

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/books` | 搜索书籍 |
| GET | `/books/{id}` | 获取书籍详情 |
| POST | `/books` | 创建书籍 |
| PUT | `/books/{id}` | 更新书籍 |
| DELETE | `/books/{id}` | 删除书籍 |
| GET | `/books/available` | 获取可借阅书籍 |
| GET | `/books/category/{category}` | 按分类获取书籍 |
| GET | `/books/popular` | 获取热门书籍 |
| GET | `/books/newest` | 获取最新书籍 |

### 借阅相关接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/borrow/{userId}/books/{bookId}` | 借阅书籍 |
| PUT | `/borrow/{userId}/books/{bookId}/return` | 归还书籍 |
| PUT | `/borrow/records/{recordId}/renew` | 续借书籍 |
| GET | `/borrow/users/{userId}/records` | 获取用户借阅记录 |
| GET | `/borrow/users/{userId}/current` | 获取当前借阅记录 |
| GET | `/borrow/users/{userId}/overdue` | 获取逾期记录 |
| GET | `/borrow/records/{recordId}` | 获取借阅记录详情 |

## 特性亮点

### 1. 完整的异常处理
- 自定义业务异常
- 全局异常处理器
- 统一的错误响应格式
- 详细的错误代码定义

### 2. 日志记录
- 请求/响应日志记录
- 操作耗时统计
- 异常堆栈记录
- 可配置的日志级别

### 3. 数据验证
- JSR-380 Bean Validation
- 自定义验证规则
- 友好的错误消息

### 4. 安全性
- 数据访问权限控制
- SQL注入防护
- XSS防护
- 敏感数据保护

### 5. 可维护性
- 清晰的代码结构
- 统一的编码规范
- 完整的注释
- 易于扩展的架构

## 数据库设计

### 主要表结构

#### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    max_borrow_count INTEGER DEFAULT 5,
    role VARCHAR(20) DEFAULT 'USER'
);
```

#### 书籍表 (books)
```sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(200) NOT NULL,
    isbn VARCHAR(50),
    publisher VARCHAR(100),
    total_copies INTEGER DEFAULT 1,
    available_copies INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE
);
```

#### 借阅记录表 (borrow_records)
```sql
CREATE TABLE borrow_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    book_id BIGINT REFERENCES books(id),
    borrow_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'BORROWED'
);
```

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok减少样板代码
- 使用MapStruct进行对象映射
- 接口优先设计

### 测试规范
- 单元测试覆盖核心业务逻辑
- 集成测试覆盖API接口
- 使用H2数据库进行测试
- 测试代码与业务代码分离

### 提交规范
- 使用Conventional Commits规范
- 提交前运行测试
- 提交前代码格式化

## 部署说明

### 生产环境配置
1. 修改数据库连接为生产环境
2. 配置HTTPS证书
3. 设置合适的JVM参数
4. 配置监控和日志收集

### Docker部署
``dockerfile
FROM openjdk:17-jdk-slim
COPY target/e-library-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 故障排除

### 常见问题

1. **数据库连接失败**
    - 检查数据库服务是否运行
    - 验证连接参数是否正确
    - 检查防火墙设置

2. **应用启动失败**
    - 检查Java版本（需要Java 17+）
    - 检查端口占用情况
    - 查看启动日志

3. **API访问失败**
    - 检查应用是否正常启动
    - 验证API路径是否正确
    - 检查请求参数格式

### 日志查看
```bash
# 查看应用日志
tail -f logs/elibrary.log

# 查看错误日志
grep ERROR logs/elibrary.log
```

## 联系方式

如有问题或建议，请联系：
- 邮箱：support@elibrary.com
- GitHub：https://github.com/your-repo/e-library

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。