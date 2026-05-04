# 邻里共享平台 - AI技术文档

> 文档版本: v1.0\
> 最后更新: 2026-05-03\
> 适用对象: AI开发助手

***

## 一、项目概览

### 1.1 项目定位

**邻里共享平台**是面向社区居民的物品共享与社区互动系统，核心目标是促进社区内资源共享、循环利用和邻里交流。

### 1.2 开发进度

- 当前进度：**92%**
- 状态：收尾优化阶段
- 核心功能：全部实现

### 1.3 技术栈

| 层级    | 技术选型            | 版本          |
| ----- | --------------- | ----------- |
| 前端框架  | Vue             | 3.x         |
| 前端语言  | TypeScript      | -           |
| 构建工具  | Vite            | -           |
| CSS框架 | TailwindCSS     | -           |
| 状态管理  | Pinia           | -           |
| 后端框架  | Spring Boot     | 3.3.5       |
| ORM框架 | Spring Data JPA | -           |
| 数据库   | MySQL           | 8.x         |
| 版本迁移  | Flyway          | -           |
| 认证方式  | JWT             | jjwt 0.12.6 |
| 实时通信  | WebSocket       | -           |
| 支付集成  | 支付宝沙箱           | -           |

***

## 二、架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层                              │
│  Vue 3 + TypeScript + Vite + TailwindCSS + Pinia           │
├─────────────────────────────────────────────────────────────┤
│                        API层                               │
│  RESTful API + WebSocket                                   │
├─────────────────────────────────────────────────────────────┤
│                        业务层                              │
│  Service + DTO + Entity                                    │
├─────────────────────────────────────────────────────────────┤
│                        数据层                              │
│  Spring Data JPA + MySQL + Flyway                          │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 目录结构

**后端目录** (`backend/demo/src/main/java/com/neighbor/`)：

```
├── auth/              # 认证模块
├── common/            # 公共组件
├── config/            # 配置类
├── controller/api/    # REST API控制器
├── dto/               # 数据传输对象
├── entity/            # JPA实体
├── enums/             # 枚举类
├── forum/             # 论坛模块
├── repository/        # 数据访问层
├── scheduler/         # 定时任务
├── service/           # 业务逻辑层
└── websocket/         # WebSocket通信
```

**前端目录** (`frontend/src/`)：

```
├── api/               # API接口定义
├── components/        # 组件
├── composables/       # 组合式函数
├── directives/        # 自定义指令
├── router/            # 路由配置
├── stores/            # Pinia状态管理
├── types/             # TypeScript类型
├── utils/             # 工具函数
└── views/             # 页面视图
```

***

## 三、核心业务模块

### 3.1 模块列表

| 模块    |  状态 | 核心功能                |
| ----- | :-: | ------------------- |
| 用户认证  |  ✅  | 手机号验证码登录、密码登录、JWT管理 |
| 物品管理  |  ✅  | 发布、编辑、上下架、图片上传、搜索筛选 |
| 借阅管理  |  ✅  | 申请、审批、确认取货、归还、提醒    |
| 支付模块  |  ✅  | 支付宝沙箱支付、押金退还        |
| 论坛模块  |  ✅  | 发帖、评论、点赞、举报         |
| 消息通知  |  ✅  | WebSocket实时推送、消息列表  |
| 评价系统  |  ✅  | 双向评价、评分、评价标签        |
| 管理员后台 |  ✅  | 用户/物品/借阅管理、举报审核     |

### 3.2 状态流转

**物品状态**：

```
DRAFT → PENDING_REVIEW → AVAILABLE → BORROWED → AVAILABLE/OFFLINE
```

**借阅状态**：

```
PENDING → APPROVED → ACTIVE → RETURN_REQUESTED → RETURNED
```

**支付状态**：

```
PENDING → PAID → REFUNDED
```

***

## 四、数据库设计

### 4.1 核心表结构

| 表名           | 关键字段                                                                   | 说明    |
| ------------ | ---------------------------------------------------------------------- | ----- |
| `users`      | id, phone, nickname, credit\_score, status, role                       | 用户表   |
| `items`      | id, name, owner\_id, price\_per\_day, deposit, status, version         | 物品表   |
| `borrows`    | id, item\_id, borrower\_id, lender\_id, start\_date, end\_date, status | 借阅记录表 |
| `payments`   | id, out\_trade\_no, status, total\_amount                              | 支付记录表 |
| `messages`   | id, type, is\_read, related\_id                                        | 消息表   |
| `reviews`    | id, rating, type, content                                              | 评价表   |
| `posts`      | id, type, status, view\_count, like\_count                             | 帖子表   |
| `comments`   | id, parent\_id, content                                                | 评论表   |
| `audit_logs` | id, review\_type, audit\_result, reporter\_id                          | 审核日志表 |

### 4.2 数据库连接信息

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/neighbor_sharing
spring.datasource.username=root
spring.datasource.password=

# 图片上传路径
app.upload.path=d:/ai练/fast/backend/demo/uploads

# JWT密钥
app.jwt.secret=change-this-secret-to-at-least-32-chars-for-production
```

***

## 五、API接口规范

### 5.1 响应格式

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

### 5.2 分页响应

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  }
}
```

### 5.3 数组字段处理规范

**核心原则**：

- 前端发送：数组 → `JSON.stringify()` → 字符串
- 后端存储：直接存储 JSON 字符串
- 后端返回：解析为 `List` → DTO
- 前端接收：直接使用 `List`

**适用字段**：`returnRequirements`, `tags`, `images`

### 5.4 认证要求

| 接口类型  | 认证要求                               |
| ----- | ---------------------------------- |
| 公开接口  | 无需认证                               |
| 用户接口  | 需要 `Authorization: Bearer <token>` |
| 管理员接口 | 需要 `ADMIN` 角色                      |

***

## 六、关键API接口

### 6.1 用户认证

| 接口                         | 方法   | 说明       |
| -------------------------- | ---- | -------- |
| `/api/auth/login/phone`    | POST | 手机号验证码登录 |
| `/api/auth/login/password` | POST | 密码登录     |
| `/api/auth/verify-code`    | POST | 获取验证码    |

### 6.2 用户管理

| 接口                        | 方法  | 说明       |
| ------------------------- | --- | -------- |
| `/api/users/me`           | GET | 获取当前用户信息 |
| `/api/users/me`           | PUT | 更新用户信息   |
| `/api/users/{id}/profile` | GET | 查看他人资料   |
| `/api/users/me/items`     | GET | 我的发布     |
| `/api/users/me/lent`      | GET | 我借出的     |
| `/api/users/me/borrowed`  | GET | 我借入的     |

### 6.3 物品管理

| 接口                         | 方法   | 说明          |
| -------------------------- | ---- | ----------- |
| `/api/items`               | GET  | 物品列表(分页、筛选) |
| `/api/items/{id}`          | GET  | 物品详情        |
| `/api/items`               | POST | 创建物品        |
| `/api/items/{id}`          | PUT  | 更新物品        |
| `/api/items/{id}/withdraw` | PUT  | 撤回发布        |

### 6.4 借阅管理

| 接口                          | 方法   | 说明     |
| --------------------------- | ---- | ------ |
| `/api/borrows`              | POST | 提交借阅申请 |
| `/api/borrows/{id}`         | GET  | 借阅详情   |
| `/api/borrows/{id}/approve` | POST | 审批申请   |
| `/api/borrows/{id}/return`  | POST | 确认归还   |

### 6.5 支付模块

| 接口                                     | 方法   | 说明            |
| -------------------------------------- | ---- | ------------- |
| `/api/payments/create`                 | POST | 创建支付（返回支付宝表单） |
| `/api/payments/notify`                 | POST | 支付宝异步通知回调     |
| `/api/payments/return`                 | GET  | 支付宝同步跳转回调     |
| `/api/payments/borrow/{borrowId}`      | GET  | 查询借阅的支付记录     |
| `/api/payments/order/{outTradeNo}`     | GET  | 按商户订单号查询支付    |
| `/api/payments/my/paid`                | GET  | 我支付的记录        |
| `/api/payments/my/received`            | GET  | 我收到的支付记录      |
| `/api/payments/{borrowId}/refund`      | POST | 押金退还          |
| `/api/payments/{borrowId}/skip-pay`    | POST | 跳过支付（测试用）     |
| `/api/payments/{borrowId}/skip-refund` | POST | 跳过退款（测试用）     |

### 6.6 论坛模块

| 接口                         | 方法   | 说明   |
| -------------------------- | ---- | ---- |
| `/api/posts`               | GET  | 帖子列表 |
| `/api/posts`               | POST | 发布帖子 |
| `/api/posts/{id}/like`     | POST | 点赞   |
| `/api/posts/{id}/comments` | POST | 评论   |
| `/api/posts/{id}/report`   | POST | 举报帖子 |

### 6.7 评价系统

| 接口                                     | 方法     | 说明      |
| -------------------------------------- | ------ | ------- |
| `/api/reviews`                         | POST   | 创建评价    |
| `/api/reviews/{id}`                    | GET    | 评价详情    |
| `/api/reviews/item/{itemId}`           | GET    | 物品评价列表  |
| `/api/reviews/user/{userId}/received`  | GET    | 用户收到的评价 |
| `/api/reviews/user/{userId}/given`     | GET    | 用户给出的评价 |
| `/api/reviews/borrow/{borrowId}/check` | GET    | 检查评价状态  |
| `/api/reviews/{id}`                    | DELETE | 删除评价    |

### 6.8 消息通知

| 接口                           | 方法   | 说明     |
| ---------------------------- | ---- | ------ |
| `/api/messages`              | GET  | 消息列表   |
| `/api/messages/unread`       | GET  | 未读消息列表 |
| `/api/messages/unread-count` | GET  | 未读消息数量 |
| `/api/messages/{id}/read`    | POST | 标记单条已读 |
| `/api/messages/read-all`     | POST | 标记全部已读 |

### 6.9 图片上传

| 接口                   | 方法   | 说明     |
| -------------------- | ---- | ------ |
| `/api/upload/images` | POST | 批量上传图片 |

### 6.11 管理员接口

| 接口                               | 方法   | 说明        |
| -------------------------------- | ---- | --------- |
| `/api/admin/users`               | GET  | 用户列表      |
| `/api/admin/items`               | GET  | 物品列表(待审核) |
| `/api/admin/items/{id}/audit`    | POST | 审核物品      |
| `/api/admin/reports`             | GET  | 举报列表      |
| `/api/admin/reports/{id}/handle` | POST | 处理举报      |

***

## 七、开发规范

### 7.1 命名规范

| 类型         | 规则                  | 示例                         |
| ---------- | ------------------- | -------------------------- |
| Entity     | 单数,首字母大写            | User, Item, Borrow         |
| Repository | Entity + Repository | UserRepository             |
| Service    | Entity + Service    | UserService                |
| Controller | Entity + Controller | UserController             |
| DTO        | 名称 + DTO/Request    | UserDTO, CreateItemRequest |

### 7.2 异常处理

```java
// 使用 BusinessException 抛出业务异常
if (item == null) {
    throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
}
```

### 7.3 事务管理

```java
@Service
@Transactional
public class ItemService {
    
    @Transactional(readOnly = true)
    public ItemDetailDTO getItemById(Long id) {
        // 只读事务
    }
    
    public Long createItem(CreateItemRequest request) {
        // 写事务
    }
}
```

***

## 八、测试数据

### 8.1 用户账号

| 手机号         | 昵称   | 角色 |
| ----------- | ---- | -- |
| 13800002026 | 李大明白 | 用户 |
| 13900001111 | 老周   | 用户 |
| 13600002222 | 小雅   | 用户 |

### 8.2 测试验证码

```
123456 (任意手机号都可登录)
```

### 8.3 测试物品

| ID | 名称        | 状态        |
| -- | --------- | --------- |
| 1  | 博世专业级电钻   | AVAILABLE |
| 2  | 黑驴轻量帐篷    | AVAILABLE |
| 3  | 富士 X100VI | AVAILABLE |
| 4  | 卡坦岛桌游     | AVAILABLE |

***

## 九、启动方式

### 9.1 后端启动

```bash
cd backend/demo
./mvnw spring-boot:run
```

### 9.2 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 9.3 访问地址

| 服务     | 地址                      |
| ------ | ----------------------- |
| 后端 API | <http://localhost:8080> |
| 前端页面   | <http://localhost:5173> |

***

## 十、注意事项

1. **数据库**：确保 MySQL 8 已安装并运行
2. **图片存储**：确保 `uploads` 目录存在且有写权限
3. **JWT密钥**：生产环境务必修改 `app.jwt.secret`
4. **跨域**：后端已配置 CORS，允许前端访问
5. **认证**：除公开接口外，其他接口需要携带 `Authorization: Bearer <token>`

***

**文档结束**
