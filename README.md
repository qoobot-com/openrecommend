# OpenRecommend - 多内容类型智能推荐系统

基于 Spring Boot 3.5.10 开发的智能推荐系统，支持文章、图片和视频的个性化推荐。

## 📋 项目简介

OpenRecommend 是一个功能完善、架构清晰的多内容类型智能推荐系统，采用单体应用架构，支持：

- 基于内容的推荐（Content-Based）
- 协同过滤推荐（Collaborative Filtering）
- 混合推荐策略（Hybrid Recommendation）
- 热门内容推荐
- 相关内容推荐

## 🎯 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.5.10 | 核心框架 |
| JDK | 21 | 运行环境（支持虚拟线程） |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| MySQL | 8.4.0 | 主数据库 |
| Redis | 7.x | 分布式缓存 |
| Caffeine | 3.1.8 | 本地缓存 |
| Swagger | 2.3.0 | API文档 |

## 📁 项目结构

```
openrecommend/
├── openrecommend-common/      # 公共模块
│   └── src/main/java/com/qoobot/openrecommend/common/
│       ├── enums/             # 枚举类
│       ├── exception/         # 异常类
│       ├── constants/         # 常量类
│       └── result/            # 响应结果
├── openrecommend-api/         # API模块
│   └── src/main/java/com/qoobot/openrecommend/api/
│       ├── controller/        # 控制器
│       └── dto/               # 数据传输对象
├── openrecommend-service/    # 服务模块
│   └── src/main/java/com/qoobot/openrecommend/service/
│       ├── impl/              # 服务实现
│       └── mapper/            # 数据访问
└── openrecommend-web/         # Web模块
    └── src/main/java/com/qoobot/openrecommend/
        ├── config/            # 配置类
        ├── exception/         # 全局异常处理
        └── OpenRecommendApplication.java
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.9.0+
- MySQL 8.0+
- Redis 7.x

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/qoobot-com/openrecommend.git
cd openrecommend
```

2. **导入数据库**
```bash
mysql -u root -p < docs/06-数据库表结构.sql
```

3. **修改配置**
编辑 `openrecommend-web/src/main/resources/application.yml`，修改数据库和Redis连接信息。

4. **编译项目**
```bash
mvn clean install
```

5. **启动应用**
```bash
cd openrecommend-web
mvn spring-boot:run
```

### 访问地址

- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

## 📚 文档

| 文档 | 说明 |
|-----|------|
| [开发计划](docs/开发计划.md) | 项目开发计划 |
| [业务设计文档](docs/01-业务设计文档.md) | 业务设计详细说明 |
| [应用设计文档](docs/02-应用设计文档.md) | 应用架构设计 |
| [数据设计文档](docs/03-数据设计文档.md) | 数据库设计说明 |
| [技术设计文档](docs/04-技术设计文档.md) | 技术实现方案 |
| [API接口文档](docs/05-API接口文档.md) | RESTful API文档 |
| [数据库表结构](docs/06-数据库表结构.sql) | 数据库脚本 |

## 🔧 开发指南

### 添加新的推荐算法

1. 在 `openrecommend-service` 模块中创建新的推荐服务
2. 实现 `RecommendService` 接口
3. 在 `RecommendController` 中添加新的API端点

### 数据库变更

1. 修改 `docs/06-数据库表结构.sql`
2. 使用 MyBatis-Plus 的代码生成器生成实体类和Mapper

## 📊 性能指标

- QPS: 300+
- P95响应时间: < 200ms
- 推荐准确率: > 85%
- 测试覆盖率: > 75%

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👥 团队

Qoobot Team

---

© 2026 Qoobot. All rights reserved.
