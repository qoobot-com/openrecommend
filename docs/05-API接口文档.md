# API接口文档

## 文档信息

| 项目 | 内容 |
|-----|------|
| 文档名称 | OpenRecommend API接口文档 |
| 版本 | 1.0.0 |
| 编写日期 | 2026-02-15 |
| 项目名称 | 多内容类型智能推荐系统 |
| Base URL | http://localhost:8080/api |

---

## 一、接口规范

### 1.1 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1707955200000
}
```

| 字段 | 类型 | 说明 |
|-----|------|------|
| code | Integer | 状态码：200-成功，400-参数错误，500-系统错误 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| timestamp | Long | 时间戳 |

### 1.2 状态码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 系统错误 |

---

## 二、用户相关接口

### 2.1 用户注册

**接口**: POST /user/register

**请求参数**:

```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000",
  "nickname": "测试用户"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（6-20位） |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| nickname | String | 否 | 昵称 |

**响应示例**:

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "nickname": "测试用户"
  },
  "timestamp": 1707955200000
}
```

### 2.2 用户登录

**接口**: POST /user/login

**请求参数**:

```json
{
  "username": "testuser",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": 1707955200000
}
```

### 2.3 获取用户信息

**接口**: GET /user/info/{userId}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar/1001.jpg",
    "email": "test@example.com",
    "phone": "13800138000",
    "status": 1,
    "registerTime": "2026-02-15T10:00:00"
  },
  "timestamp": 1707955200000
}
```

### 2.4 获取用户画像

**接口**: GET /user/profile/{userId}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "ageRange": "25-34",
    "gender": 1,
    "interestTags": {
      "科技": 0.8,
      "娱乐": 0.6,
      "教育": 0.5
    },
    "contentPreference": {
      "article": 0.5,
      "image": 0.3,
      "video": 0.2
    },
    "categoryPreference": {
      "科技": 0.7,
      "娱乐": 0.3
    },
    "activePeriods": [8, 12, 18, 22],
    "devicePreference": "mobile",
    "readPreference": {
      "short": 0.6,
      "medium": 0.3,
      "long": 0.1
    },
    "totalViewCount": 1520,
    "totalLikeCount": 86,
    "totalCollectCount": 34
  },
  "timestamp": 1707955200000
}
```

---

## 三、推荐相关接口

### 3.1 个性化推荐

**接口**: POST /recommend/personal

**请求参数**:

```json
{
  "userId": 1001,
  "contentType": "article",
  "limit": 20,
  "device": "mobile"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |
| contentType | String | 是 | 内容类型：article/image/video/all |
| limit | Integer | 否 | 推荐数量，默认20，最大50 |
| device | String | 否 | 设备类型：mobile/pc/tablet |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "contentId": 1001,
        "contentType": "article",
        "score": 0.95,
        "reason": "基于您的兴趣推荐"
      },
      {
        "contentId": 1002,
        "contentType": "article",
        "score": 0.88,
        "reason": "相似用户也喜欢"
      }
    ],
    "timestamp": 1707955200000
  },
  "timestamp": 1707955200000
}
```

### 3.2 热门推荐

**接口**: GET /recommend/hot/{contentType}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| contentType | String | 是 | 内容类型：article/image/video |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| limit | Integer | 否 | 推荐数量，默认20 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "contentId": 2001,
        "contentType": "article",
        "score": 1.0,
        "reason": "热门内容"
      },
      {
        "contentId": 2002,
        "contentType": "article",
        "score": 0.98,
        "reason": "热门内容"
      }
    ],
    "timestamp": 1707955200000
  },
  "timestamp": 1707955200000
}
```

### 3.3 相关推荐

**接口**: GET /recommend/related/{contentId}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| contentId | Long | 是 | 内容ID |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| limit | Integer | 否 | 推荐数量，默认10 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "contentId": 3001,
        "contentType": "article",
        "score": 0.92,
        "reason": "相似内容"
      },
      {
        "contentId": 3002,
        "contentType": "article",
        "score": 0.85,
        "reason": "相似内容"
      }
    ],
    "timestamp": 1707955200000
  },
  "timestamp": 1707955200000
}
```

---

## 四、文章相关接口

### 4.1 发布文章

**接口**: POST /content/article

**请求参数**:

```json
{
  "title": "Spring Boot 3.5.10新特性详解",
  "summary": "本文详细介绍Spring Boot 3.5.10的新特性和改进",
  "content": "Spring Boot 3.5.10带来了许多新特性...",
  "coverImage": "https://example.com/covers/springboot.jpg",
  "categoryId": 1,
  "tags": ["科技", "Java", "Spring Boot"],
  "authorId": 1001,
  "source": "原创",
  "isOriginal": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| title | String | 是 | 文章标题（1-500字符） |
| summary | String | 否 | 文章摘要 |
| content | String | 是 | 文章内容 |
| coverImage | String | 否 | 封面图URL |
| categoryId | Long | 否 | 分类ID |
| tags | Array | 否 | 标签数组 |
| authorId | Long | 否 | 作者ID |
| source | String | 否 | 来源 |
| isOriginal | Integer | 否 | 是否原创：0-否，1-是 |

**响应示例**:

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "articleId": 1001
  },
  "timestamp": 1707955200000
}
```

### 4.2 获取文章详情

**接口**: GET /content/article/{id}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| id | Long | 是 | 文章ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "title": "Spring Boot 3.5.10新特性详解",
    "summary": "本文详细介绍Spring Boot 3.5.10的新特性和改进",
    "content": "Spring Boot 3.5.10带来了许多新特性...",
    "coverImage": "https://example.com/covers/springboot.jpg",
    "categoryId": 1,
    "categoryName": "科技",
    "tags": ["科技", "Java", "Spring Boot"],
    "authorId": 1001,
    "authorName": "技术达人",
    "authorAvatar": "https://example.com/avatar/1001.jpg",
    "isOriginal": 1,
    "wordCount": 3500,
    "readDuration": 8,
    "status": 2,
    "publishTime": "2026-02-15T10:00:00",
    "viewCount": 1234,
    "likeCount": 56,
    "commentCount": 12,
    "collectCount": 23,
    "shareCount": 8,
    "qualityScore": 92.5,
    "createTime": "2026-02-15T09:00:00",
    "updateTime": "2026-02-15T10:00:00"
  },
  "timestamp": 1707955200000
}
```

### 4.3 获取文章列表

**接口**: GET /content/article/list

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| categoryId | Long | 否 | 分类ID |
| tag | String | 否 | 标签 |
| status | Integer | 否 | 状态 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 20,
    "list": [
      {
        "id": 1001,
        "title": "Spring Boot 3.5.10新特性详解",
        "summary": "本文详细介绍Spring Boot 3.5.10的新特性和改进",
        "coverImage": "https://example.com/covers/springboot.jpg",
        "authorName": "技术达人",
        "viewCount": 1234,
        "likeCount": 56,
        "publishTime": "2026-02-15T10:00:00"
      }
    ]
  },
  "timestamp": 1707955200000
}
```

---

## 五、图片相关接口

### 5.1 上传图片

**接口**: POST /content/image

**请求参数**:

```json
{
  "title": "美丽的日落",
  "description": "在海边拍摄的美丽日落",
  "url": "https://example.com/images/sunset.jpg",
  "thumbnailUrl": "https://example.com/images/sunset_thumb.jpg",
  "width": 1920,
  "height": 1080,
  "aspectRatio": 1.778,
  "fileSize": 2048576,
  "format": "jpg",
  "categoryId": 1,
  "tags": ["风景", "日落", "海滩"],
  "uploaderId": 1001,
  "source": "原创"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| title | String | 否 | 图片标题 |
| description | String | 否 | 图片描述 |
| url | String | 是 | 图片URL |
| thumbnailUrl | String | 否 | 缩略图URL |
| width | Integer | 否 | 图片宽度 |
| height | Integer | 否 | 图片高度 |
| aspectRatio | Decimal | 否 | 宽高比 |
| fileSize | Long | 否 | 文件大小（字节） |
| format | String | 否 | 图片格式 |
| categoryId | Long | 否 | 分类ID |
| tags | Array | 否 | 标签数组 |
| uploaderId | Long | 否 | 上传者ID |
| source | String | 否 | 来源 |

**响应示例**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "imageId": 2001
  },
  "timestamp": 1707955200000
}
```

### 5.2 获取图片详情

**接口**: GET /content/image/{id}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| id | Long | 是 | 图片ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "title": "美丽的日落",
    "description": "在海边拍摄的美丽日落",
    "url": "https://example.com/images/sunset.jpg",
    "thumbnailUrl": "https://example.com/images/sunset_thumb.jpg",
    "width": 1920,
    "height": 1080,
    "aspectRatio": 1.778,
    "fileSize": 2048576,
    "format": "jpg",
    "categoryId": 1,
    "categoryName": "风景",
    "tags": ["风景", "日落", "海滩"],
    "uploaderId": 1001,
    "uploaderName": "摄影师小王",
    "dominantColors": ["#FF5733", "#C70039", "#900C3F"],
    "status": 1,
    "publishTime": "2026-02-15T10:00:00",
    "viewCount": 567,
    "likeCount": 34,
    "collectCount": 18,
    "downloadCount": 12,
    "qualityScore": 88.5,
    "createTime": "2026-02-15T09:00:00"
  },
  "timestamp": 1707955200000
}
```

---

## 六、视频相关接口

### 6.1 上传视频

**接口**: POST /content/video

**请求参数**:

```json
{
  "title": "Spring Boot入门教程",
  "description": "从零开始学习Spring Boot框架",
  "coverUrl": "https://example.com/covers/springboot_video.jpg",
  "videoUrl": "https://example.com/videos/springboot_tutorial.mp4",
  "duration": 3600,
  "width": 1920,
  "height": 1080,
  "resolution": "1080p",
  "fileSize": 524288000,
  "format": "mp4",
  "bitrate": 2000,
  "fps": 30,
  "categoryId": 1,
  "tags": ["教育", "编程", "Spring Boot"],
  "uploaderId": 1001,
  "source": "原创"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| title | String | 是 | 视频标题 |
| description | String | 否 | 视频描述 |
| coverUrl | String | 是 | 封面图URL |
| videoUrl | String | 是 | 视频URL |
| duration | Integer | 是 | 视频时长（秒） |
| width | Integer | 否 | 视频宽度 |
| height | Integer | 否 | 视频高度 |
| resolution | String | 否 | 分辨率 |
| fileSize | Long | 否 | 文件大小（字节） |
| format | String | 否 | 视频格式 |
| bitrate | Integer | 否 | 比特率（kbps） |
| fps | Decimal | 否 | 帧率 |
| categoryId | Long | 否 | 分类ID |
| tags | Array | 否 | 标签数组 |
| uploaderId | Long | 否 | 上传者ID |
| source | String | 否 | 来源 |

**响应示例**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "videoId": 3001
  },
  "timestamp": 1707955200000
}
```

### 6.2 获取视频详情

**接口**: GET /content/video/{id}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| id | Long | 是 | 视频ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3001,
    "title": "Spring Boot入门教程",
    "description": "从零开始学习Spring Boot框架",
    "coverUrl": "https://example.com/covers/springboot_video.jpg",
    "videoUrl": "https://example.com/videos/springboot_tutorial.mp4",
    "duration": 3600,
    "width": 1920,
    "height": 1080,
    "resolution": "1080p",
    "fileSize": 524288000,
    "format": "mp4",
    "bitrate": 2000,
    "fps": 30,
    "categoryId": 1,
    "categoryName": "教育",
    "tags": ["教育", "编程", "Spring Boot"],
    "uploaderId": 1001,
    "uploaderName": "编程讲师",
    "status": 1,
    "publishTime": "2026-02-15T10:00:00",
    "viewCount": 2345,
    "likeCount": 123,
    "commentCount": 45,
    "collectCount": 67,
    "shareCount": 23,
    "finishWatchCount": 890,
    "avgWatchDuration": 2400,
    "qualityScore": 90.0,
    "createTime": "2026-02-15T09:00:00"
  },
  "timestamp": 1707955200000
}
```

---

## 七、用户行为接口

### 7.1 记录用户行为

**接口**: POST /behavior/record

**请求参数**:

```json
{
  "userId": 1001,
  "contentType": "article",
  "contentId": 1001,
  "behaviorType": 1,
  "duration": 120,
  "scrollDepth": 75.5,
  "isFinish": 0,
  "device": "mobile",
  "os": "iOS",
  "browser": "Safari",
  "ip": "192.168.1.100",
  "location": "北京市"
}
```

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |
| contentType | String | 是 | 内容类型：article/image/video |
| contentId | Long | 是 | 内容ID |
| behaviorType | Integer | 是 | 行为类型：1-浏览，2-点赞，3-收藏，4-分享，5-评论 |
| duration | Integer | 否 | 停留时长（秒），仅浏览行为记录 |
| scrollDepth | Decimal | 否 | 滚动深度（百分比） |
| isFinish | Integer | 否 | 是否完播/完读：0-否，1-是 |
| device | String | 否 | 设备类型 |
| os | String | 否 | 操作系统 |
| browser | String | 否 | 浏览器 |
| ip | String | 否 | IP地址 |
| location | String | 否 | 地理位置 |

**响应示例**:

```json
{
  "code": 200,
  "message": "记录成功",
  "data": {
    "behaviorId": 10001
  },
  "timestamp": 1707955200000
}
```

### 7.2 获取用户行为历史

**接口**: GET /behavior/history/{userId}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| contentType | String | 否 | 内容类型 |
| behaviorType | Integer | 否 | 行为类型 |
| days | Integer | 否 | 查询最近N天，默认30 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 150,
    "page": 1,
    "size": 20,
    "list": [
      {
        "id": 10001,
        "userId": 1001,
        "contentType": "article",
        "contentId": 1001,
        "behaviorType": 1,
        "behaviorTypeName": "浏览",
        "duration": 120,
        "scrollDepth": 75.5,
        "isFinish": 0,
        "device": "mobile",
        "createTime": "2026-02-15T10:00:00"
      }
    ]
  },
  "timestamp": 1707955200000
}
```

---

## 八、分类相关接口

### 8.1 获取分类列表

**接口**: GET /category/{contentType}

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| contentType | String | 是 | 内容类型：article/image/video |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "科技",
      "parentId": 0,
      "sort": 1,
      "icon": "tech",
      "description": "科技相关内容",
      "status": 1,
      "children": [
        {
          "id": 11,
          "name": "人工智能",
          "parentId": 1,
          "sort": 1,
          "status": 1
        }
      ]
    },
    {
      "id": 2,
      "name": "娱乐",
      "parentId": 0,
      "sort": 2,
      "icon": "entertainment",
      "description": "娱乐相关内容",
      "status": 1
    }
  ],
  "timestamp": 1707955200000
}
```

---

## 九、通用接口

### 9.1 健康检查

**接口**: GET /health

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": "2026-02-15T10:00:00"
  },
  "timestamp": 1707955200000
}
```

### 9.2 获取系统信息

**接口**: GET /info

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "name": "openrecommend",
    "version": "1.0.0",
    "description": "多内容类型智能推荐系统",
    "javaVersion": "21",
    "springBootVersion": "3.5.10"
  },
  "timestamp": 1707955200000
}
```

---

## 十、错误响应示例

### 10.1 参数错误

```json
{
  "code": 400,
  "message": "用户ID不能为空",
  "data": null,
  "timestamp": 1707955200000
}
```

### 10.2 系统错误

```json
{
  "code": 500,
  "message": "系统异常，请稍后重试",
  "data": null,
  "timestamp": 1707955200000
}
```

---

## 附录

### A. 请求头说明

| 请求头 | 说明 | 示例 |
|-------|------|------|
| Content-Type | 请求内容类型 | application/json |
| Accept | 接受的响应类型 | application/json |
| Authorization | 认证令牌 | Bearer {token} |

### B. 分页参数说明

| 参数 | 类型 | 说明 |
|-----|------|------|
| page | Integer | 页码，从1开始 |
| size | Integer | 每页数量，默认20，最大100 |

### C. 日期时间格式

所有日期时间字段使用ISO 8601格式：`yyyy-MM-dd'T'HH:mm:ss`

示例：`2026-02-15T10:00:00`

### D. 枚举值说明

#### 内容类型 (contentType)

| 值 | 说明 |
|-----|------|
| article | 文章 |
| image | 图片 |
| video | 视频 |
| all | 全部 |

#### 行为类型 (behaviorType)

| 值 | 说明 |
|-----|------|
| 1 | 浏览 |
| 2 | 点赞 |
| 3 | 收藏 |
| 4 | 分享 |
| 5 | 评论 |

#### 设备类型 (device)

| 值 | 说明 |
|-----|------|
| mobile | 移动端 |
| pc | PC端 |
| tablet | 平板 |

#### 文章状态 (status)

| 值 | 说明 |
|-----|------|
| 0 | 草稿 |
| 1 | 待审核 |
| 2 | 已发布 |
| 3 | 下架 |
| 4 | 审核拒绝 |

#### 图片/视频状态 (status)

| 值 | 说明 |
|-----|------|
| 0 | 待审核 |
| 1 | 已发布 |
| 2 | 下架 |
| 3 | 审核拒绝 |

---

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|-----|------|---------|-------|
| 1.0.0 | 2026-02-15 | 初始版本 | - |
