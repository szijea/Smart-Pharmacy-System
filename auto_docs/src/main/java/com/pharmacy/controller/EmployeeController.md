```markdown
# EmployeeController 文档

## 1. 文件功能

`EmployeeController` 是一个使用 Spring Boot 框架的 REST 控制器，主要用于处理药店的员工信息。该控制器提供了获取所有员工信息、根据 ID 获取特定员工、创建新员工、更新现有员工、删除员工以及根据角色获取员工列表等功能。

## 2. 核心类/方法

### 2.1 类 EmployeeController

#### 2.1.1 方法 getAllEmployees()

- **作用**: 获取所有员工的信息。
- **URL**: `/api/employees`
- **HTTP 方法**: `GET`
- **参数**: 无
- **返回值**: `ResponseEntity<List<Employee>>` - 包含所有员工的列表及 HTTP 状态码 200 OK。

#### 2.1.2 方法 getEmployeeById(Integer id)

- **作用**: 根据员工 ID 获取特定员工的信息。
- **URL**: `/api/employees/{id}`
- **HTTP 方法**: `GET`
- **参数**:
  - `@PathVariable Integer id`: 员工的 ID。
- **返回值**: `ResponseEntity<Employee>` - 包含特定员工信息及 HTTP 状态码，若员工不存在返回 404 NOT FOUND。

#### 2.1.3 方法 createEmployee(Employee employee)

- **作用**: 创建新的员工信息。
- **URL**: `/api/employees`
- **HTTP 方法**: `POST`
- **参数**:
  - `@RequestBody Employee employee`: 新员工的详细信息。
- **返回值**: `ResponseEntity<?>` - 若创建成功返回新员工信息及 HTTP 状态码 201 CREATED，如果用户名已存在返回错误信息及 409 CONFLICT。

#### 2.1.4 方法 updateEmployee(Integer id, Employee employee)

- **作用**: 更新现有员工的信息。
- **URL**: `/api/employees/{id}`
- **HTTP 方法**: `PUT`
- **参数**:
  - `@PathVariable Integer id`: 要更新员工的 ID。
  - `@RequestBody Employee employee`: 更新后的员工信息。
- **返回值**: `ResponseEntity<?>` - 若更新成功返回更新后的员工信息及 HTTP 状态码 200 OK，如果员工不存在返回错误信息及 404 NOT FOUND。

#### 2.1.5 方法 deleteEmployee(Integer id)

- **作用**: 根据员工 ID 删除员工信息。
- **URL**: `/api/employees/{id}`
- **HTTP 方法**: `DELETE`
- **参数**:
  - `@PathVariable Integer id`: 要删除员工的 ID。
- **返回值**: `ResponseEntity<Void>` - 删除成功返回 HTTP 状态码 204 NO CONTENT，如果员工不存在返回 404 NOT FOUND。

#### 2.1.6 方法 getEmployeesByRole(Integer roleId)

- **作用**: 根据角色 ID 获取员工列表。
- **URL**: `/api/employees/role/{roleId}`
- **HTTP 方法**: `GET`
- **参数**:
  - `@PathVariable Integer roleId`: 角色ID。
- **返回值**: `ResponseEntity<List<Employee>>` - 包含特定角色员工的列表及 HTTP 状态码 200 OK。

## 3. 注意事项

- 确保已经正确配置了 Spring Boot 和相关依赖（Spring Web）。
- 在使用这些 API 之前，后端服务应当已经启动并运行。
- 该控制器依赖于 `EmployeeService`，确保该服务中的方法已经正确实现。
- 对于异常处理，该控制器使用了 ResponseEntity 来返回相应的 HTTP 状态码，客户端需要根据返回的状态码来处理可能出现的异常情况（如 404 NOT FOUND，409 CONFLICT）。
- 在创建和更新员工信息时，需要确保传递的 `Employee` 对象符合服务端的验证要求。
- 在使用 `@Autowired` 注入 `EmployeeService` 时，需要确保有相应的 EmployeeService 实例可以被 Spring 容器管理。
```
```