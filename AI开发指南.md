# 邻里共享平台 - AI 开发指南

> 本文档为后续 AI 开发提供完整的项目架构、开发规范和任务清单

---

## 📋 项目概览

### 技术栈
- **后端**: Spring Boot 3.3.5 + JPA + MySQL 8 + Flyway
- **前端**: Vue 3 + Vite + TypeScript + Pinia
- **认证**: JWT (jjwt 0.12.6)
- **ID策略**: 自增 ID
- **图片存储**: 本地存储

### 项目结构

```
d:\ai练\fast\
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 6个页面组件（已完成）
│   │   ├── router/             # 路由配置（已完成）
│   │   ├── components/         # 公共组件
│   │   └── main.ts
│   └── package.json
│
├── backend/demo/                # Spring Boot 后端
│   ├── src/main/java/com/neighbor/
│   │   ├── entity/             # ✅ 实体类（13个）
│   │   ├── repository/         # ✅ Repository接口（13个）
│   │   ├── service/            # ⚠️ 待实现
│   │   ├── controller/api/     # ⚠️ 待实现
│   │   ├── dto/                # ⚠️ 待实现
│   │   ├── enums/              # ✅ 枚举类（5个）
│   │   ├── auth/               # ✅ 认证模块（已完成）
│   │   ├── common/             # ✅ 公共组件
│   │   └── config/             # ✅ 配置类
│   │
│   ├── src/main/resources/
│   │   ├── db/migration/       # ✅ Flyway迁移脚本（2个）
│   │   └── application.properties
│   │
│   └── uploads/                # 图片上传目录
│       ├── avatars/
│       └── items/
│
└── 原型/                        # 设计文档
    ├── 项目分析与下一步.md
    └── 参考文档.md
```

---

## 🗄️ 数据库设计

### ER 关系图

```
用户(User) 1--N 物品(Item)
用户(User) 1--N 借阅(Borrow) [作为借入者]
用户(User) 1--N 借阅(Borrow) [作为借出者]
物品(Item) 1--N 图片(ItemImage)
物品(Item) 1--N 借阅(Borrow)
社区(Community) 1--N 用户(User)
社区(Community) 1--N 物品(Item)
分类(Category) 1--N 物品(Item)
用户(User) 1--N 帖子(Post)
帖子(Post) 1--N 评论(Comment)
用户(User) 1--N 点赞(Like)
借阅(Borrow) 1--1 评价(Review)
```

### 核心表结构

#### 1. users (用户表)
```sql
- id: BIGINT (主键,自增)
- phone: VARCHAR(20) (唯一,手机号)
- nickname: VARCHAR(50)
- avatar: VARCHAR(500)
- community_id: BIGINT (外键)
- credit_score: DECIMAL(3,2) (信用分0-10)
- borrow_count: INT (借入次数)
- lend_count: INT (借出次数)
- co2_saved: INT (碳减排克数)
- status: VARCHAR(20) (ACTIVE/BANNED/DELETED)
```

#### 2. items (物品表)
```sql
- id: BIGINT (主键,自增)
- name: VARCHAR(100)
- category_id: BIGINT (外键)
- owner_id: BIGINT (外键,所有者)
- community_id: BIGINT (外键)
- description: TEXT
- story: TEXT
- price_per_day: DECIMAL(10,2)
- deposit: DECIMAL(10,2)
- credit_required: DECIMAL(3,2)
- return_requirements: TEXT (JSON数组)
- status: VARCHAR(20) (DRAFT/AVAILABLE/BORROWED/OFFLINE/DELETED)
- tags: VARCHAR(500) (JSON数组)
- borrow_count: INT
- view_count: INT
```

#### 3. borrows (借阅记录表)
```sql
- id: BIGINT (主键,自增)
- item_id: BIGINT (外键)
- borrower_id: BIGINT (外键,借入者)
- lender_id: BIGINT (外键,借出者)
- start_date: DATE
- end_date: DATE
- actual_return_date: DATE
- purpose: VARCHAR(500)
- status: VARCHAR(20) (PENDING/APPROVED/ACTIVE/RETURNED/OVERDUE/REJECTED/CANCELLED)
- remind_count: INT
```

---

## 🎯 开发任务清单

### P0 - 核心闭环（优先完成）

#### 1. 用户模块 Service & Controller
**文件位置**: 
- Service: `backend/demo/src/main/java/com/neighbor/service/UserService.java`
- Controller: `backend/demo/src/main/java/com/neighbor/controller/api/UserController.java`

**需要实现的接口**:
```
GET  /api/users/me              # 获取当前用户信息
PUT  /api/users/me              # 更新用户信息
GET  /api/users/{id}/profile    # 查看他人资料
GET  /api/users/me/items        # 我的发布
GET  /api/users/me/lent         # 我借出的
GET  /api/users/me/borrowed     # 我借入的
GET  /api/users/me/pending      # 待处理申请
GET  /api/users/me/reviews      # 我的评价
```

**DTO 类**:
```java
// UserDTO.java
public record UserDTO(
    Long id,
    String nickname,
    String avatar,
    String phone,
    String communityName,
    String building,
    BigDecimal creditScore,
    Integer borrowCount,
    Integer lendCount,
    Integer co2Saved
) {}

// UserStatsDTO.java
public record UserStatsDTO(
    Integer lentCount,
    Integer borrowedCount,
    Integer pendingCount,
    Integer dueSoonCount,
    Integer todayCo2Saved
) {}
```

#### 2. 物品模块 Service & Controller
**文件位置**:
- Service: `backend/demo/src/main/java/com/neighbor/service/ItemService.java`
- Controller: `backend/demo/src/main/java/com/neighbor/controller/api/ItemController.java`

**需要实现的接口**:
```
GET  /api/items                 # 物品列表(分页、筛选)
GET  /api/items/search          # 搜索物品
GET  /api/items/{id}            # 物品详情
POST /api/items                 # 创建物品
PUT  /api/items/{id}            # 更新物品
PUT  /api/items/{id}/withdraw   # 撤回发布
POST /api/items/draft           # 保存草稿
GET  /api/items/{id}/similar    # 相似推荐
```

**DTO 类**:
```java
// ItemListDTO.java (列表项)
public record ItemListDTO(
    Long id,
    String name,
    String mainImage,
    BigDecimal pricePerDay,
    String status,
    OwnerDTO owner,
    String locationText,
    List<String> tags
) {}

// ItemDetailDTO.java (详情)
public record ItemDetailDTO(
    Long id,
    String name,
    String description,
    String story,
    List<ImageDTO> images,
    RulesDTO rules,
    String status,
    Integer borrowCount,
    OwnerDTO owner
) {}

// CreateItemRequest.java
public record CreateItemRequest(
    String name,
    Long categoryId,
    String description,
    String story,
    List<String> images,
    RulesDTO rules,
    List<String> tags,
    LocationDTO location
) {}
```

#### 3. 借阅模块 Service & Controller
**文件位置**:
- Service: `backend/demo/src/main/java/com/neighbor/service/BorrowService.java`
- Controller: `backend/demo/src/main/java/com/neighbor/controller/api/BorrowController.java`

**需要实现的接口**:
```
POST /api/borrows               # 提交借阅申请
GET  /api/borrows/{id}          # 借阅详情
POST /api/borrows/{id}/approve  # 审批申请
POST /api/borrows/{id}/remind   # 提醒归还
POST /api/borrows/{id}/return   # 确认归还
```

**DTO 类**:
```java
// BorrowRequest.java
public record BorrowRequest(
    Long itemId,
    LocalDate startDate,
    LocalDate endDate,
    String purpose,
    Boolean agreeTerms
) {}

// BorrowDTO.java
public record BorrowDTO(
    Long id,
    ItemDTO item,
    UserDTO borrower,
    UserDTO lender,
    DatesDTO dates,
    String status,
    String purpose
) {}

// ApproveRequest.java
public record ApproveRequest(
    Boolean approved,
    String reason
) {}
```

#### 4. 公共接口 Controller
**文件位置**: `backend/demo/src/main/java/com/neighbor/controller/api/PublicController.java`

**需要实现的接口**:
```
GET  /api/communities           # 社区列表
GET  /api/categories            # 分类列表
GET  /api/stats/carbon          # 碳排放统计
```

#### 5. 图片上传 Controller
**文件位置**: `backend/demo/src/main/java/com/neighbor/controller/api/UploadController.java`

**需要实现的接口**:
```
POST /api/upload/images         # 上传图片(支持多文件)
```

**实现要点**:
```java
@PostMapping("/upload/images")
public ApiResponse<List<ImageUploadResult>> uploadImages(
    @RequestParam("files") MultipartFile[] files
) {
    // 1. 校验文件类型(jpg/png/webp)
    // 2. 校验文件大小(最大10MB)
    // 3. 生成唯一文件名(UUID)
    // 4. 保存到 app.upload.path 配置的目录
    // 5. 返回相对URL路径
}
```

---

### P1 - 功能完善

#### 6. 搜索筛选功能
- 在 ItemService 中实现多条件筛选
- 支持关键词搜索、分类筛选、社区筛选
- 支持排序（最新、最热、距离）

#### 7. 草稿箱功能
- 实现草稿保存和加载
- 支持草稿列表查询

#### 8. 待处理数量角标
- 在用户信息中返回待处理申请数量
- 实现未读消息计数

#### 9. 提醒归还功能
- 实现提醒次数限制（每天最多3次）
- 记录最后提醒时间

---

### P2 - 扩展功能

#### 10. 论坛模块
**文件位置**:
- Service: `backend/demo/src/main/java/com/neighbor/service/PostService.java`
- Controller: `backend/demo/src/main/java/com/neighbor/controller/api/PostController.java`

**接口**:
```
GET  /api/posts                 # 帖子列表
POST /api/posts                 # 发布帖子
POST /api/posts/{id}/like       # 点赞
POST /api/posts/{id}/comments   # 评论
GET  /api/announcements         # 社区公告
GET  /api/leaderboard/green     # 绿色榜单
GET  /api/skills/exchange       # 技能交换
```

#### 11. 评价系统
- 借阅完成后双方互评
- 评价影响信用分

#### 12. 消息通知
- 系统消息
- 借阅状态变更通知

---

## 📝 开发规范

### 1. 命名规范

#### Java 类命名
- Entity: `User`, `Item`, `Borrow` (单数,首字母大写)
- Repository: `UserRepository`, `ItemRepository`
- Service: `UserService`, `ItemService`
- Controller: `UserController`, `ItemController`
- DTO: `UserDTO`, `CreateItemRequest`, `BorrowDTO`

#### 方法命名
- 查询: `get`, `find`, `list`
- 创建: `create`, `add`
- 更新: `update`, `modify`
- 删除: `delete`, `remove`
- 业务操作: `approve`, `reject`, `remind`, `return`

### 2. 异常处理

```java
// 使用 BusinessException 抛出业务异常
if (item == null) {
    throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
}

// 使用 ErrorCode 枚举
throw new BusinessException(ErrorCode.ITEM_NOT_AVAILABLE);
```

### 3. 事务管理

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

### 4. 认证鉴权

```java
@RestController
@RequestMapping("/api/items")
public class ItemController {
    
    @GetMapping
    public ApiResponse<Page<ItemListDTO>> getItems(...) {
        // 公开接口,无需认证
    }
    
    @PostMapping
    public ApiResponse<Long> createItem(...) {
        // 需要认证,从 SecurityContext 获取当前用户
        AuthUser currentUser = getCurrentUser();
    }
    
    private AuthUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (AuthUser) auth.getPrincipal();
    }
}
```

### 5. 分页规范

```java
// Controller
@GetMapping
public ApiResponse<Page<ItemListDTO>> getItems(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
    // ...
}

// 响应格式
{
    "code": 0,
    "message": "OK",
    "data": {
        "content": [...],
        "totalElements": 100,
        "totalPages": 5,
        "number": 0,
        "size": 20
    }
}
```

### 6. JSON 字段处理

```java
// Entity 中存储 JSON 数组
@Column(columnDefinition = "TEXT")
private String returnRequirements; // ["已消毒", "保持原样"]

// DTO 中转换为 List
public record RulesDTO(
    BigDecimal pricePerDay,
    BigDecimal deposit,
    BigDecimal creditRequired,
    List<String> returnRequirements
) {}

// 使用工具类转换
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static List<String> parseList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    public static String toJson(List<String> list) {
        try {
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
```

---

## 🔧 配置说明

### application.properties 关键配置

```properties
# 数据库(需要先创建)
spring.datasource.url=jdbc:mysql://localhost:3306/neighbor_sharing

# JWT密钥(生产环境需要修改)
app.jwt.secret=change-this-secret-to-at-least-32-chars-for-production

# 图片上传路径
app.upload.path=d:/ai练/fast/backend/demo/uploads
```

### 数据库初始化

```bash
# 1. 创建数据库
CREATE DATABASE neighbor_sharing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 启动后端,Flyway会自动执行迁移脚本
# V1__Init_Schema.sql - 创建所有表
# V2__Init_Data.sql - 插入初始数据
```

---

## 📊 测试数据

### 已插入的测试数据

**用户**:
- 手机号: 13800002026, 昵称: 李大明白
- 手机号: 13900001111, 昵称: 老周
- 手机号: 13600002222, 昵称: 小雅

**物品**:
- 博世专业级电钻 (ID: 1)
- 黑驴轻量帐篷 (ID: 2)
- 富士 X100VI (ID: 3)
- 卡坦岛桌游 (ID: 4)

**测试验证码**: 123456 (任意手机号都可登录)

---

## 🚀 快速开始

### 后端启动

```bash
cd backend/demo
./mvnw spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 访问地址

- 后端: http://localhost:8080
- 前端: http://localhost:5173
- API文档: http://localhost:8080/swagger-ui.html (需添加Swagger依赖)

---

## 📌 注意事项

1. **数据库**: 确保MySQL 8已安装并运行,先创建数据库
2. **图片存储**: 确保 `uploads` 目录存在且有写权限
3. **JWT密钥**: 生产环境务必修改 `app.jwt.secret`
4. **跨域**: 后端已配置CORS,允许前端访问
5. **认证**: 除公开接口外,其他接口需要携带 `Authorization: Bearer <token>`

---

## 📚 参考文档

- [项目分析与下一步.md](./原型/项目分析与下一步.md) - 完整接口契约
- [参考文档.md](./原型/参考文档.md) - 页面功能分析
- [Spring Boot 3 文档](https://docs.spring.io/spring-boot/docs/3.3.5/reference/html/)
- [Vue 3 文档](https://vuejs.org/)

---

## ✅ 完成状态

| 模块 | Entity | Repository | Service | Controller | DTO |
|------|--------|-----------|---------|-----------|-----|
| 认证 | - | - | ✅ | ✅ | ✅ |
| 用户 | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| 物品 | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| 借阅 | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| 论坛 | ✅ | ✅ | ⚠️ | ⚠️ | ⚠️ |
| 公共 | ✅ | ✅ | - | ⚠️ | ⚠️ |

**图例**: ✅ 已完成 | ⚠️ 待实现 | - 不需要

---

**祝开发顺利！** 🎉
