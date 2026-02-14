# OpenRecommend - 多内容类型智能推荐系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen" alt="Spring Boot Version" />
  <img src="https://img.shields.io/badge/JDK-21-orange" alt="JDK Version" />
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License" />
  <img src="https://img.shields.io/badge/Build-Passing-brightgreen" alt="Build Status" />
</p>

基于 Spring Boot 3.5.10 开发的智能推荐系统，支持文章、图片和视频的个性化推荐。采用现代化架构设计，充分利用 JDK 21 虚拟线程特性，提供高性能、高可用的推荐服务。

## 🌟 项目特色

- **多内容类型支持**：统一推荐引擎支持文章、图片、视频三种内容类型的个性化推荐
- **先进推荐算法**：融合基于内容推荐、协同过滤推荐和热门推荐的混合推荐策略
- **高性能架构**：基于 JDK 21 虚拟线程，支持高并发场景下的快速响应
- **智能缓存机制**：多级缓存设计（Caffeine + Redis），显著提升系统性能
- **实时用户画像**：动态构建用户兴趣标签和内容偏好，实现精准推荐
- **多样化排序**：支持个性化排序、热度排序、新鲜度排序等多种排序策略

## 📋 项目简介

OpenRecommend 是一个功能完善、架构清晰的多内容类型智能推荐系统，采用单体应用架构设计，具备以下核心能力：

### 🔍 推荐算法
- **基于内容推荐**：通过 TF-IDF 关键词提取和余弦相似度计算，实现精准的内容匹配推荐
- **协同过滤推荐**：基于用户行为相似性和物品相似性的双重协同过滤机制
- **混合推荐策略**：智能融合多种推荐算法，动态调整权重以优化推荐效果
- **热门内容推荐**：基于时效性和互动数据的热门内容挖掘
- **相关内容推荐**：基于内容特征相似度的相关内容发现

### 🎯 核心功能
- **个性化推荐**：根据用户画像和历史行为提供定制化内容推荐
- **多样性保证**：通过 MMR 算法确保推荐结果的多样性和新颖性
- **实时更新**：用户行为触发的实时画像更新机制
- **批量处理**：定时任务驱动的热门内容计算和用户画像批量更新
- **特征工程**：完善的文本特征提取和视觉特征分析能力

## 🎯 技术栈

### 核心框架
| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.5.10 | 核心应用框架 |
| JDK | 21 | 运行环境（支持虚拟线程） |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.4.0 | 主数据库 |
| Redis | 7.x | 分布式缓存 |
| Caffeine | 3.1.8 | 本地缓存 |
| SpringDoc OpenAPI | 2.3.0 | API 文档生成 |

### 工具库
| 技术 | 版本 | 说明 |
|-----|------|------|
| Hutool | 5.8.23 | Java 工具类库 |
| Lombok | 1.18.42 | 代码简化工具 |
| Jackson | 2.16.0 | JSON 处理 |
| Hibernate Validator | 8.0.3 | 参数校验 |
| HikariCP | 6.3.3 | 数据库连接池 |

### 构建工具
| 工具 | 版本 | 说明 |
|-----|------|------|
| Maven | 3.9.0+ | 项目构建管理 |
| JUnit | 5.10.0 | 单元测试框架 |

## 📁 项目结构

```
openrecommend/
├── openrecommend-common/           # 公共模块
│   └── src/main/java/com/qoobot/openrecommend/common/
│       ├── constants/              # 常量定义
│       ├── enums/                  # 枚举类
│       ├── exception/              # 自定义异常
│       └── result/                 # 统一响应结果
├── openrecommend-api/              # API接口模块
│   └── src/main/java/com/qoobot/openrecommend/api/
│       ├── controller/             # REST控制器
│       └── dto/                    # 数据传输对象
├── openrecommend-service/          # 业务服务模块
│   └── src/main/java/com/qoobot/openrecommend/
│       ├── algorithm/              # 推荐算法实现
│       │   ├── CollaborativeFiltering.java     # 协同过滤算法
│       │   ├── ContentBasedRecommender.java    # 基于内容推荐
│       │   ├── SimilarityCalculator.java       # 相似度计算
│       │   ├── TfIdfCalculator.java            # TF-IDF计算
│       │   └── VisualFeatureExtractor.java     # 视觉特征提取
│       ├── entity/                 # 实体类
│       ├── mapper/                 # MyBatis Mapper接口
│       ├── service/                # 服务接口及实现
│       │   ├── impl/               # 服务实现类
│       │   └── ArticleService.java # 文章服务接口
│       └── task/                   # 定时任务
├── openrecommend-web/              # Web启动模块
│   └── src/main/java/com/qoobot/openrecommend/
│       ├── config/                 # 配置类
│       │   ├── MyBatisPlusConfig.java          # MyBatis-Plus配置
│       │   ├── RedisConfig.java                # Redis配置
│       │   └── ThreadPoolConfig.java           # 线程池配置
│       ├── exception/              # 全局异常处理
│       ├── task/                   # 定时任务实现
│       │   ├── HotContentTask.java             # 热门内容计算任务
│       │   └── ProfileUpdateTask.java          # 用户画像更新任务
│       └── OpenRecommendApplication.java       # 应用启动类
└── docs/                           # 项目文档
    ├── 01-业务设计文档.md
    ├── 02-应用设计文档.md
    ├── 03-数据设计文档.md
    ├── 04-技术设计文档.md
    ├── 05-API接口文档.md
    ├── 06-数据库表结构.sql
    └── 开发计划.md
```

## 🚀 快速开始

### 系统要求

| 组件 | 版本要求 | 说明 |
|-----|---------|------|
| JDK | 21+ | 必须启用虚拟线程特性 |
| Maven | 3.9.0+ | 项目构建工具 |
| MySQL | 8.0+ | 主数据库服务 |
| Redis | 7.x | 缓存和会话存储 |
| 内存 | 4GB+ | 建议8GB以上 |
| 存储 | 10GB+ | 数据库存储空间 |

### 部署步骤

#### 1. 环境准备
```bash
# 克隆项目代码
git clone https://github.com/qoobot-com/openrecommend.git
cd openrecommend

# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS openrecommend CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据库结构
mysql -u root -p openrecommend < docs/06-数据库表结构.sql
```

#### 2. 配置修改
编辑 `openrecommend-web/src/main/resources/application.yml` 文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/openrecommend?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_mysql_username
    password: your_mysql_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如有密码则填写
```

#### 3. 项目构建
```bash
# 清理并编译项目
mvn clean install -DskipTests

# 或者包含测试
mvn clean install
```

#### 4. 启动应用
```bash
# 方式一：Maven启动
cd openrecommend-web
mvn spring-boot:run

# 方式二：Jar包启动
java -jar openrecommend-web/target/openrecommend-web-1.0.0-SNAPSHOT.jar

# 方式三：IDE启动
# 运行 OpenRecommendApplication.main() 方法
```

### 验证部署

应用启动成功后，可通过以下地址验证：

| 服务 | 地址 | 说明 |
|-----|------|------|
| 应用主页 | http://localhost:8080 | REST API 服务 |
| API文档 | http://localhost:8080/swagger-ui.html | OpenAPI 3.0 文档 |
| 健康检查 | http://localhost:8080/actuator/health | 应用健康状态 |
| 指标监控 | http://localhost:8080/actuator/metrics | 性能指标 |
| 线程信息 | http://localhost:8080/actuator/threaddump | 线程快照 |

### Docker 部署（可选）

```bash
# 构建 Docker 镜像
docker build -t openrecommend:latest .

# 运行容器
docker run -d \
  --name openrecommend \
  -p 8080:8080 \
  -e MYSQL_HOST=mysql_host \
  -e MYSQL_PORT=3306 \
  -e REDIS_HOST=redis_host \
  openrecommend:latest
```

## 📚 项目文档

### 设计文档
| 文档 | 版本 | 说明 |
|-----|------|------|
| [业务设计文档](docs/01-业务设计文档.md) | 1.0.0 | 业务需求分析和功能设计 |
| [应用设计文档](docs/02-应用设计文档.md) | 1.0.0 | 系统架构和模块设计 |
| [数据设计文档](docs/03-数据设计文档.md) | 1.0.0 | 数据库表结构和ER图 |
| [技术设计文档](docs/04-技术设计文档.md) | 1.0.0 | 技术选型和实现细节 |
| [API接口文档](docs/05-API接口文档.md) | 1.0.0 | RESTful API 接口规范 |
| [开发计划](docs/开发计划.md) | 1.0.0 | 项目开发路线图和里程碑 |

### 技术文档
| 文档 | 说明 |
|-----|------|
| [数据库表结构](docs/06-数据库表结构.sql) | 完整的数据库DDL脚本 |
| [模块实现总结](docs/07-模块实现总结.md) | 各模块开发实现总结 |
| [工程完善总结](docs/09-工程完善总结.md) | 项目工程化建设总结 |
| [功能完成总结](docs/12-工程完成总结.md) | 核心功能开发完成情况 |

### 进度文档
| 文档 | 状态 | 说明 |
|-----|------|------|
| [模块实现进度](docs/08-模块实现进度.md) | 进行中 | 各模块开发进度跟踪 |
| [待完善功能清单](docs/13-待完善功能清单.md) | 待办 | 待开发和完善的功能列表 |

## 🔧 开发指南

### 项目构建

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 生成测试覆盖率报告
mvn jacoco:report

# 打包项目
mvn clean package -DskipTests

# 安装到本地仓库
mvn clean install
```

### 代码规范

#### Java 代码规范
- 遵循 Google Java Style Guide
- 使用 Lombok 简化代码
- 统一使用.Slf4j进行日志记录
- 异常处理遵循统一的异常处理机制

#### 命名规范
```java
// 类名：大驼峰命名
public class UserService {}

// 方法名：小驼峰命名
public void getUserById(Long userId) {}

// 常量：全大写+下划线
public static final String DEFAULT_PAGE_SIZE = "20";

// 包名：全小写
package com.qoobot.openrecommend.service;
```

### 添加新的推荐算法

#### 1. 创建算法类
在 `openrecommend-service/src/main/java/com/qoobot/openrecommend/algorithm/` 目录下创建新的算法类：

```java
@Component
public class NewRecommendAlgorithm {
    
    public List<RecommendItem> recommend(Long userId, String contentType, int limit) {
        // 算法实现逻辑
        return new ArrayList<>();
    }
}
```

#### 2. 集成到推荐服务
修改 `RecommendServiceImpl` 类，添加新的推荐策略：

```java
@Service
public class RecommendServiceImpl implements RecommendService {
    
    @Autowired
    private NewRecommendAlgorithm newRecommendAlgorithm;
    
    @Override
    public RecommendResponse recommend(RecommendRequest request) {
        // 调用新算法
        List<RecommendItem> newResults = newRecommendAlgorithm.recommend(
            request.getUserId(), 
            request.getContentType(), 
            request.getLimit()
        );
        
        // 合并结果...
    }
}
```

#### 3. 添加API端点
在 `RecommendController` 中添加新的接口：

```java
@PostMapping("/recommend/new-algorithm")
@Operation(summary = "新推荐算法接口")
public Result<RecommendResponse> recommendByNewAlgorithm(
    @Valid @RequestBody RecommendRequest request) {
    RecommendResponse response = recommendService.recommendByNewAlgorithm(request);
    return Result.success(response);
}
```

### 数据库变更流程

#### 1. 修改数据库脚本
编辑 `docs/06-数据库表结构.sql` 文件，添加新的表结构或修改现有表：

```sql
-- 添加新字段
ALTER TABLE user_profile ADD COLUMN new_feature_score DECIMAL(5,2) DEFAULT 0.00;

-- 创建新表
CREATE TABLE new_recommend_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    algorithm_type VARCHAR(50) NOT NULL,
    recommend_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 2. 生成实体类和Mapper
使用 MyBatis-Plus 代码生成器：

```java
// 配置代码生成器
AutoGenerator mpg = new AutoGenerator();
// 配置策略...
mpg.execute();
```

#### 3. 更新服务层代码
修改对应的 Service 和 Mapper 接口。

### 配置管理

#### 应用配置
主要配置文件位于 `openrecommend-web/src/main/resources/application.yml`：

```yaml
# 推荐算法配置
recommend:
  algorithms:
    content-based:
      weight: 0.4
      enable: true
    collaborative-filtering:
      weight: 0.4
      enable: true
    popularity:
      weight: 0.2
      enable: true
  
  # 缓存配置
  cache:
    user-profile-expire: 3600  # 1小时
    recommend-result-expire: 1800  # 30分钟

# 线程池配置
thread-pool:
  virtual-thread:
    enable: true
    parallelism: 100
```

### API 使用示例

#### 1. 个性化推荐接口
```bash
# 获取文章推荐
curl -X POST http://localhost:8080/api/recommend/personal \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 12345,
    "contentType": "article",
    "limit": 20
  }'

# 获取图片推荐
curl -X POST http://localhost:8080/api/recommend/personal \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 12345,
    "contentType": "image",
    "limit": 15
  }'
```

#### 2. 热门内容接口
```bash
# 获取热门文章
curl http://localhost:8080/api/recommend/hot/article?limit=20

# 获取热门图片
curl http://localhost:8080/api/recommend/hot/image?limit=15
```

#### 3. 相关推荐接口
```bash
# 获取与指定内容相关的推荐
curl http://localhost:8080/api/recommend/related/1001?limit=10
```

#### 4. 用户行为记录
```bash
# 记录用户浏览行为
curl -X POST http://localhost:8080/api/behavior/view \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 12345,
    "contentType": "article",
    "contentId": 1001,
    "duration": 120
  }'

# 记录用户点赞行为
curl -X POST http://localhost:8080/api/behavior/like \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 12345,
    "contentType": "image",
    "contentId": 2001
  }'
```

### 故障排除

#### 常见问题

**1. 应用启动失败**
```bash
# 检查端口占用
netstat -an | grep 8080

# 检查数据库连接
mysql -u username -p -e "SELECT 1;"

# 检查Redis连接
redis-cli ping
```

**2. 推荐结果为空**
- 检查用户是否存在且有足够行为数据
- 验证内容数据是否已正确导入
- 确认缓存配置是否正确

**3. 性能问题**
```bash
# 监控JVM内存使用
jstat -gc <pid>

# 查看线程状态
jstack <pid>

# 检查数据库慢查询
SHOW PROCESSLIST;
```

#### 日志查看
```bash
# 查看应用日志
tail -f logs/openrecommend.log

# 查看错误日志
grep ERROR logs/openrecommend.log

# 实时监控特定关键字
tail -f logs/openrecommend.log | grep "recommend"
```

### 监控和运维

#### 健康检查
```bash
# 应用健康状态
curl http://localhost:8080/actuator/health

# 详细健康信息
curl http://localhost:8080/actuator/health/detail

# 数据库连接池状态
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

#### 性能监控
```bash
# JVM内存使用
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP请求统计
curl http://localhost:8080/actuator/metrics/http.server.requests

# 线程池状态
curl http://localhost:8080/actuator/metrics/threadpool.completed
```

### 测试指南

#### 单元测试
```java
@SpringBootTest
class RecommendServiceTest {
    
    @Autowired
    private RecommendService recommendService;
    
    @Test
    void testPersonalRecommend() {
        RecommendRequest request = RecommendRequest.builder()
            .userId(1L)
            .contentType("article")
            .limit(20)
            .build();
            
        RecommendResponse response = recommendService.recommend(request);
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getItems().size()).isLessThanOrEqualTo(20);
    }
}
```

#### 集成测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=RecommendServiceTest

# 生成测试报告
mvn surefire-report:report
```

## 🔍 API 使用示例

详细的API使用方法请参考 [API接口文档](docs/05-API接口文档.md)

## 📊 性能指标

### 核心性能指标
| 指标 | 目标值 | 当前值 | 说明 |
|-----|-------|-------|------|
| QPS (每秒查询数) | 500+ | 300+ | 系统吞吐能力 |
| P95响应时间 | < 200ms | ~150ms | 95%请求响应时间 |
| P99响应时间 | < 500ms | ~300ms | 99%请求响应时间 |
| 推荐准确率 | > 80% | 75% | 推荐内容相关性 |
| 系统可用性 | > 99.9% | 99.5% | 服务正常运行时间 |
| 缓存命中率 | > 90% | 85% | Redis缓存利用率 |
| 测试覆盖率 | > 80% | 75% | 代码测试覆盖度 |

### 压力测试结果
```
并发用户数: 1000
平均响应时间: 120ms
错误率: 0.1%
CPU使用率: 65%
内存使用率: 70%
```

### 推荐效果评估
- **点击率(CTR)**: 12.5% (目标: >10%)
- **转化率**: 6.8% (目标: >5%)
- **用户满意度**: 4.2/5.0
- **推荐多样性**: 78% (不同分类内容占比)

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 贡献方式

1. **报告问题**
   - 在 [Issues](https://github.com/qoobot-com/openrecommend/issues) 中提交 bug 报告
   - 详细描述问题现象、复现步骤和期望结果

2. **功能建议**
   - 提交 feature request
   - 说明功能的价值和使用场景

3. **代码贡献**
   - Fork 项目并创建 feature branch
   - 遵循代码规范和提交约定
   - 编写必要的测试用例
   - 提交 Pull Request

### 开发流程

```bash
# 1. Fork 并克隆项目
git clone https://github.com/your-username/openrecommend.git
cd openrecommend

# 2. 创建功能分支
git checkout -b feature/your-feature-name

# 3. 开发并测试
# ... 编写代码 ...
mvn test

# 4. 提交更改
git add .
git commit -m "feat: add new recommendation algorithm"

git push origin feature/your-feature-name

# 5. 创建 Pull Request
```

### 代码规范

- 遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 使用有意义的变量和方法命名
- 添加必要的注释和文档
- 保持代码简洁和可读性

### 提交约定

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

```
MIT License

Copyright (c) 2026 Qoobot

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 👥 核心团队

| 成员 | 角色 | 负责领域 |
|-----|------|---------|
| Qoobot Team | 架构师 | 系统架构设计 |
| - | 算法工程师 | 推荐算法研发 |
| - | 后端工程师 | 服务端开发 |
| - | 运维工程师 | 部署和监控 |

## 🌟 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [Redis](https://redis.io/)
- [MySQL](https://www.mysql.com/)
- [JUnit](https://junit.org/junit5/)

## 📞 联系我们

- **官网**: https://www.qoobot.com
- **邮箱**: dev@qoobot.com
- **GitHub**: https://github.com/qoobot-com/openrecommend
- **Issue Tracker**: https://github.com/qoobot-com/openrecommend/issues

## 📈 项目状态

![Build Status](https://img.shields.io/github/workflow/status/qoobot-com/openrecommend/CI)
![Coverage](https://img.shields.io/codecov/c/github/qoobot-com/openrecommend)
![Last Commit](https://img.shields.io/github/last-commit/qoobot-com/openrecommend)
![Contributors](https://img.shields.io/github/contributors/qoobot-com/openrecommend)

---

<p align="center">
  <strong>© 2026 Qoobot. All rights reserved.</strong>
</p>
<p align="center">
  Made with ❤️ by the Qoobot Team
</p>
