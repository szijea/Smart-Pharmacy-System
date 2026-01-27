```markdown
# ExportController.java 文档

## 1. 文件功能

`ExportController` 类是一个使用Spring Boot框架的控制层组件（Controller），主要用于处理药房管理系统中仪表板报表的导出功能。该控制器提供了一个端点 `/api/export/dashboard-report`，用于将仪表板数据以JSON格式下载。

## 2. 核心类/方法

### 类 `ExportController`

- **功能**: 控制器类，提供报表导出的API接口。
- **注解**:
  - `@RestController`: 标记为Spring的控制器组件，用于处理HTTP请求。
  - `@RequestMapping("/api/export")`: 设置该控制器处理的所有请求的基准路径。

### 方法 `exportDashboardReport()`

- **功能**: 处理导出仪表板报表的请求。
- **返回值**: `ResponseEntity<byte[]>`: 返回一个包含报表数据的HTTP响应实体。
- **参数**: 无
- **流程**:
  1. 通过 `dashboardService.getExportData()` 获取报表数据。
  2. 将获取的数据转换为JSON格式。
  3. 创建包含时间戳的文件名。
  4. 设置HTTP响应头，包括内容类型和文件下载名。
  5. 返回包含报表数据的响应实体。

### 方法 `convertToJson(Map<String, Object> data)`

- **功能**: 将Map数据结构转换为JSON格式的字符串。
- **返回值**: `String`: JSON格式的字符串。
- **参数**: `Map<String, Object> data`: 需要转换的键值对数据。
- **注意事项**: 实际项目中建议使用成熟的JSON库，如Jackson。

### 方法 `escapeJsonString(String str)`

- **功能**: 转义JSON字符串中的特殊字符。
- **返回值**: `String`: 转义后的字符串。
- **参数**: `String str`: 需要转义的原始字符串。

## 3. 注意事项

- **依赖**: 使用Spring Boot框架，需要确保项目中包含相应的依赖。
- **异常处理**: 代码中通过 try-catch 捕获了异常，但简单的错误处理只是返回了一个包含错误信息的JSON。在生产环境中，应该更详细地处理异常，并记录日志。
- **安全性**: 文件名创建时未考虑防止路径注入等安全问题，确保前端正确处理下载的文件。
- **性能**: 如果报表数据量大，应考虑异步处理和资源消耗管理。
```
