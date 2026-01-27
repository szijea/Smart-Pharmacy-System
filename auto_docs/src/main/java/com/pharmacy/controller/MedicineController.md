```markdown
# MedicineController.java 文档

## 1. 文件功能

`MedicineController` 类是药品管理服务的控制器，主要用于处理药品相关的请求。它定义了一系列 RESTful API，包括查询药品列表、搜索药品、创建药品、获取单个药品详情等。此外，该控制器还包含了健康检查和测试端点。

## 2. 核心类/方法

### 2.1 类注解说明

- `@RestController`: 标记为 Spring MVC 的 REST 控制器。
- `@RequestMapping("/api/medicines")`: 设置了该控制器处理请求的基本路径。

### 2.2 主要方法

#### 2.2.1 健康检查

- `@GetMapping("/health")`
- **功能**: 返回服务状态、名称、时间戳和端口信息。
- **返回值**: `ResponseEntity<Map<String, Object>>`: 包含服务状态信息的响应实体。

#### 2.2.2 测试端点

- `@GetMapping("/test")`
- **功能**: 返回服务测试信息。
- **返回值**: `ResponseEntity<String>`: 包含服务测试信息的字符串。

#### 2.2.3 搜索药品

- `@GetMapping("/search")`
- **功能**: 根据关键词和分类搜索药品。
- **参数**:
  - `@RequestParam(required = false) String keyword`: 搜索关键词（非必须）。
  - `@RequestParam(required = false) String category`: 分类（非必须）。
  - `@RequestParam(defaultValue = "1") int page`: 分页页码（默认值为1）。
  - `@RequestParam(defaultValue = "100") int size`: 每页大小（默认值为100）。
- **返回值**: `ResponseEntity<Map<String, Object>>`: 包含药品搜索结果的响应实体。

#### 2.2.4 获取所有药品

- `@GetMapping`
- **功能**: 获取所有药品列表。
- **参数**:
  - `@RequestParam(defaultValue = "0") int page`: 分页页码（默认值为0）。
  - `@RequestParam(defaultValue = "10") int size`: 每页大小（默认值为10）。
- **返回值**: `ResponseEntity<Page<Medicine>>`: 包含分页药品信息的响应实体。

#### 2.2.5 创建药品

- `@PostMapping`
- **功能**: 创建新的药品记录。
- **参数**: `@RequestBody Medicine medicine`: 要创建的药品对象。
- **返回值**: `ResponseEntity<?>`: 成功时返回创建的药品对象，失败时返回错误信息。

#### 2.2.6 获取单个药品

- `@GetMapping("/{id}")
- **功能**: 根据药品 ID 获取单个药品的详细信息。
- **参数**: `@PathVariable String id`: 药品 ID。
- **返回值**: `ResponseEntity<Medicine>`: 包含药品信息的响应实体。

#### 2.2.7 搜索包含库存的药品

- `@GetMapping("/search-with-stock")`
- **功能**: 搜索药品并包含库存信息。
- **参数**: 同 `2.2.3`。
- **返回值**: `ResponseEntity<Map<String, Object>>`: 包含药品及库存信息的响应实体。

## 3. 注意事项

- **依赖**: 该控制器依赖于 `MedicineService` 和 `InventoryService`，确保这些服务已经正确实现并提供所需的方法。
- **异常处理**: 控制器中的方法应捕获可能出现的异常，并提供合适的错误响应。
- **健康检查和测试端点**: 应确保这些端点可用于监控和测试服务。
- **路径映射**: 在部署时，应确保 URL 路径与实际应用部署环境相匹配。
- **数据完整性**: 在创建药品时，应确保处理可能的唯一约束冲突。
- **接口变动**: 如果删除了与 `memberService` 相关的方法和引用，应确保前端应用同步更新。
```
```