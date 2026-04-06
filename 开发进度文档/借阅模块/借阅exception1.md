为什么要在 Item.java 中添加了与 ItemImage 的一对多关系，与数据库有关嘛，是开始没设计好对吗

## 解释
### 数据库设计是正确的
数据库层面已经设计好了关系：

- items 表 - 物品主表
- item_images 表 - 图片表，有 item_id 外键指向 items.id
```
-- 数据库中已经存在这个关系
CREATE TABLE item_images (
    id BIGINT PRIMARY KEY,
    item_id BIGINT,  -- 外键指向 items 表
    url VARCHAR(500),
    is_main BOOLEAN,
    ...
);
```
### JPA 实体关系映射
问题所在 ：

- 数据库有外键关系 ✅
- 但 Java 实体类没有声明这个关系 ❌
在代码中使用 ：

```
// BorrowService.java 第215行
borrow.getItem().getImages()  // 报错！Item 类
没有 getImages() 方法
```
添加关系映射后 ：

```
@OneToMany(mappedBy = "item", cascade = 
CascadeType.ALL, fetch = FetchType.LAZY)
@OrderBy("sortOrder ASC")
private List<ItemImage> images = new 
ArrayList<>();
```
这样 JPA 就知道如何从数据库加载关联的图片数据。

### 不是设计问题
这是正常的开发流程：

层面 状态 说明 数据库设计 ✅ 正确 外键关系已存在 Entity 实体 ⚠️ 不完整 缺少关系映射 业务代码 ❌ 报错 无法访问关联数据

类比 ：

- 数据库像是"仓库"，已经按规则存放了数据
- JPA 实体像是"地图"，需要告诉代码如何找到关联的数据
- 添加 @OneToMany 就是在地图上画出"物品→图片"的路线
### 总结
这不是设计问题，而是 实现细节遗漏 。数据库设计是正确的，只是 Java 实体类需要补充关系映射，才能在业务代码中访问关联数据。