## sensitive-word 库说明

### 库的作用

**sensitive-word** 是一个开源的 Java 敏感词过滤库，核心功能：

| 功能 | 说明 |
|------|------|
| **敏感词检测** | 判断文本是否包含敏感词 |
| **敏感词查找** | 找出文本中所有敏感词 |
| **敏感词替换** | 用 `*` 等字符替换敏感词 |
| **高性能** | 基于 DFA 算法，毫秒级响应 |

### 与数据库表的关系

```
┌─────────────────────────┐      ┌─────────────────────────┐
│   sensitive_words 表     │      │   sensitive-word 库      │
│   (MySQL 数据库)         │      │   (内存 DFA 树)          │
├─────────────────────────┤      ├─────────────────────────┤
│ id, word, category,     │ ───→ │ 敏感词列表加载到内存     │
│ severity, status        │      │ 构建高效的查找树         │
└─────────────────────────┘      └─────────────────────────┘
           ↑                                │
           │                                ↓
    管理员通过后台管理              实际审核时使用
    (增删改查敏感词)               (毫秒级检测)
```

### 工作流程

1. **管理员操作**：通过后台界面添加/删除敏感词 → 存入 `sensitive_words` 表
2. **缓存刷新**：定时任务每分钟从表读取启用的敏感词 → 加载到 sensitive-word 内存
3. **内容审核**：用户发帖时 → sensitive-word 在内存中快速检测

### 为什么这样设计？

| 方案 | 优点 | 缺点 |
|------|------|------|
| **纯数据库 LIKE 查询** | 简单 | 性能差，O(n) 复杂度 |
| **sensitive-word 库** | 高性能，O(1) 查找 | 需要内存缓存 |

### 代码对应关系

```java
// 1. 从数据库读取敏感词
List<String> words = sensitiveWordRepository.findAllEnabledWords();

// 2. 加载到 sensitive-word 内存
IWordDeny wordDeny = new IWordDeny() {
    @Override
    public List<String> deny() {
        return words;  // 返回数据库中的敏感词列表
    }
};

// 3. 构建检测器
sensitiveWordBs = SensitiveWordBs.newInstance()
        .wordDeny(wordDeny)
        .init();

// 4. 使用检测器审核内容
List<String> foundWords = sensitiveWordBs.findAll(text);  // 毫秒级
```

**总结**：`sensitive_words` 表是敏感词的**持久化存储**，sensitive-word 库是**高性能检测引擎**，两者配合使用实现可管理、高性能的敏感词审核。