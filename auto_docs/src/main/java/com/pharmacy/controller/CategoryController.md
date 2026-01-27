# CategoryController Java Doc

## 1. 文件概述

### 所属模块
药品分类管理模块。

### 设计目的
`CategoryController` 类旨在提供RESTful API接口，用于药品分类的增删改查操作，保证药品分类数据的有效管理和便捷访问。

## 2. 核心类

### 类的继承关系
- `CategoryController` 继承自 `Object`

### 注解含义
- `@RestController`: 标注该类为一个控制器组件，用于处理HTTP请求。
- `@RequestMapping("/api/categories")`: 设置该控制器处理的所有请求的基础路径。
- `@CrossOrigin(origins = "http://localhost:8080")`: 允许来自指定源（此处为本地服务器的8080端口）的跨域请求。

## 3. 方法详情

以下为 `CategoryController` 类的公共方法说明：

### getAllCategories
获取所有分类。

#### 参数
无

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含分类列表的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### testConnection
数据库连接测试。

#### 参数
无

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含数据库连接状态的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### getCategoryById
根据ID获取分类。

#### 参数
- `@PathVariable Integer id`: 分类ID

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含分类详情的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### createCategory
创建分类。

#### 参数
- `@RequestBody Category category`: 要创建的分类对象。

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含新创建分类的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### updateCategory
更新分类。

#### 参数
- `@PathVariable Integer id`: 分类ID
- `@RequestBody Category category`: 更新后的分类对象。

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含更新后分类的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### deleteCategory
删除分类。

#### 参数
- `@PathVariable Integer id`: 分类ID

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 确认删除操作的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### getCategoriesByParentId
根据父分类ID获取子分类。

#### 参数
- `@PathVariable Integer parentId`: 父分类ID

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含子分类列表的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### searchCategories
搜索分类。

#### 参数
- `@RequestParam String keyword`: 搜索关键词

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含搜索结果列表的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### getTopLevelCategories
获取一级分类。

#### 参数
无

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含一级分类列表的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

### checkCategoryNameExists
检查分类名称是否存在。

#### 参数
- `@RequestParam String categoryName`: 分类名称

#### 返回值
- `ResponseEntity<Map<String, Object>>`: 包含存在状态的响应实体。

#### 抛出的异常
可能抛出 `Exception`。

## 4. 业务逻辑

- **获取所有分类**: 通过调用 `CategoryService` 的 `findAll` 方法从数据库中检索所有分类。
- **创建分类**: 验证分类名称不存在后，通过 `CategoryService` 的 `save` 方法保存新分类。
- **更新分类**: 根据ID查找分类，确保分类名称不重复，然后通过 `CategoryService` 更新分类。
- **删除分类**: 根据ID删除分类，删除前检查分类是否存在。
- **其他**: 提供根据ID、父分类ID、搜索关键词获取分类的功能。

## 5. 依赖说明

- 引用的其他类: `Category`, `CategoryService`, `Result`
- 配置文件: 无特定提及
- 第三方库: 使用 `Spring Boot` 相关注解和类，如 `@RestController`, `@RequestMapping`, `ResponseEntity` 等。