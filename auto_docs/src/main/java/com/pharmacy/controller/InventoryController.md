```markdown
# InventoryController 文档

## 1. 文件功能

`InventoryController` 是一个使用 Spring Boot 框架编写的控制器类，其主要用于处理药品库存相关的 HTTP 请求。该控制器类提供了以下功能：

- 获取即将过期的药品列表。
- 获取所有库存药品的列表。
- 获取库存量低的药品列表。

## 2. 核心类/方法

### 2.1 类

- `InventoryController`
  - 该类是一个 REST 控制器，以 `/api/inventory` 为基础路径，处理与药品库存相关的请求。

### 2.2 方法

- `getExpiringSoon()`
  - **作用**: 获取即将过期的药品列表。
  - **HTTP 方法**: `GET`
  - **路径**: `/api/inventory/expiring-soon`
  - **参数**: 无
  - **返回值**: `ResponseEntity<?>` 返回一个包含状态码、消息和数据的响应实体。
- `getAllInventory()`
  - **作用**: 获取所有库存药品的列表。
  - **HTTP 方法**: `GET`
  - **路径**: `/api/inventory`
  - **参数**: 无
  - **返回值**: `ResponseEntity<?>` 返回一个包含状态码、消息和数据的响应实体。数据使用 `InventoryDTO` 避免懒加载代理序列化失败。
- `getLowStock()`
  - **作用**: 获取库存量低的药品列表。
  - **HTTP 方法**: `GET`
  - **路径**: `/api/inventory/low-stock`
  - **参数**: 无
  - **返回值**: `ResponseEntity<?>` 返回一个包含状态码、消息和数据的响应实体。

### 2.3 服务类

- `InventoryService`
  - `inventoryService.getExpiringSoon()`: 获取即将过期的库存列表。
  - `inventoryService.findAllWithMedicineDTO()`: 查找所有库存，并使用 DTO 对象返回结果。
  - `inventoryService.getLowStock()`: 获取库存量低的列表。

## 3. 注意事项

- **依赖**: 该控制器类依赖于 `InventoryService`，确保该服务类已被正确实现并提供所需的方法。
- **异常处理**: 方法中使用了 try-catch 块，用于捕获可能发生的异常，并以统一的方式返回错误信息。确保所有预期外的异常都能被合理地捕获和处理。
- **序列化问题**: 使用 DTO (`InventoryDTO`) 避免了 Hibernate 懒加载带来的序列化问题。
- **安全性**: 确保对外暴露的 API 接口有适当的权限控制，防止未授权访问。
```
``` 
以上文档详细介绍了 `InventoryController` 类的功能、核心方法及使用时的注意事项，以便于其他开发者阅读和使用。
```