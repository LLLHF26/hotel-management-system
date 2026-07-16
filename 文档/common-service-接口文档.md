# common-service 文档

> common-service 不暴露 HTTP 接口，以 JAR 包形式被其他 5 个微服务引用。它提供统一的返回格式、错误码、枚举、DTO、Feign 声明、异常处理、工具类。

---

## 一、统一返回格式 `Result<T>`

所有服务对外返回的 JSON 结构：

```java
public class Result<T> {
    private int code;      // 状态码
    private String msg;    // 提示信息
    private T data;        // 数据体，可为 null
}
```

### 1.1 成功响应快捷方法

```java
Result.ok()                          // { code:200, msg:"成功", data:null }
Result.ok(T data)                    // { code:200, msg:"成功", data:data }
Result.ok(String msg, T data)        // { code:200, msg:msg,  data:data }
```

### 1.2 失败响应快捷方法

```java
Result.fail(String msg)              // { code:500, msg:msg, data:null }
Result.fail(int code, String msg)    // { code:code, msg:msg, data:null }
```

### 1.3 分页封装 `PageResult<T>`

```java
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> records;
}
```

---

## 二、统一错误码

| 枚举名 | code | 含义 | 使用场景 |
|--------|------|------|---------|
| `SUCCESS` | 200 | 成功 | 所有正常返回 |
| `BAD_REQUEST` | 400 | 参数错误 | 参数校验失败、业务规则不满足 |
| `UNAUTHORIZED` | 401 | 未登录 | Token 缺失、过期、无效 |
| `FORBIDDEN` | 403 | 无权限 | 角色无权限访问该接口 |
| `NOT_FOUND` | 404 | 资源不存在 | ID 查询无结果 |
| `CONFLICT` | 409 | 数据冲突 | 用户名已存在、房间已被预订 |
| `TOO_MANY_REQUESTS` | 429 | 请求过频 | 触发限流 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 | 未预期异常 |

```java
public enum ErrorCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String defaultMsg;
}
```

---

## 三、公共枚举

### 3.1 房间状态 `RoomStatus`

```java
public enum RoomStatus {
    空闲中,    // 可售
    预订中,    // 已预订未入住
    入住中,    // 有客
    待清洁中,  // 退房后等待打扫
    打扫中,    // 保洁清理中
    维修中;    // 故障维修

    // 允许跳转的目标状态集合
    public boolean canTransitionTo(RoomStatus target) { ... }
}
```

**合法跳转矩阵：**

| | 空闲中 | 预订中 | 入住中 | 待清洁中 | 打扫中 | 维修中 |
|--------|:---:|:---:|:---:|:---:|:---:|:---:|
| **空闲中** | - | ✔ | - | - | - | ✔ |
| **预订中** | ✔ | - | ✔ | - | - | - |
| **入住中** | - | - | - | ✔ | - | - |
| **待清洁中** | - | - | - | - | ✔ | - |
| **打扫中** | ✔ | - | - | - | - | - |
| **维修中** | ✔ | - | - | - | - | - |

### 3.2 订单状态 `OrderStatus`

```java
public enum OrderStatus {
    待支付,  // 刚创建，未付款
    已支付,  // 付款完成，等待入住
    已入住,  // 客人已办理入住
    已完成,  // 已退房，订单完结
    已取消,  // 取消预订
    已退款;   // 已完成退款
}
```

### 3.3 用户角色 `UserRole`

```java
public enum UserRole {
    ADMIN,       // 管理员 — 全部权限
    FRONT_DESK,  // 前台 — 入住/退房/房态/收银
    CLEANER;     // 保洁员 — 查看打扫任务
}
```

### 3.4 会员等级 `MemberLevel`

```java
public enum MemberLevel {
    NORMAL,   // 普通
    SILVER,   // 银卡（累计消费 ≥ 5000）
    GOLD,     // 金卡（累计消费 ≥ 20000）
    DIAMOND;  // 钻卡（累计消费 ≥ 50000）
}
```

### 3.5 支付方式 `PaymentMethod`

```java
public enum PaymentMethod {
    CASH("现金"),
    ALIPAY("支付宝"),
    WECHAT("微信支付"),
    CARD("银行卡");
}
```

### 3.6 订单来源 `OrderSource`

```java
public enum OrderSource {
    ONLINE("线上预订"),
    WALK_IN("到店散客"),
    FRONT_DESK("前台代订");
}
```

---

## 四、公共 DTO

### 4.1 房间状态变更请求 `RoomStatusChangeDTO`

```java
// 服务间调用时传递，声明在 common-service 的 feign 包中
public class RoomStatusChangeDTO {
    @NotNull
    private String status;          // 目标状态
    private String reason;          // 变更原因（维修时必填）
}
```

### 4.2 打扫任务摘要 DTO `CleaningTaskDTO`

```java
// 跨 user-service / room-service 使用
public class CleaningTaskDTO {
    private Long taskId;
    private Long roomId;
    private String roomNumber;
    private Long cleanerId;
    private String cleanerName;
    private String status;          // 打扫中 / 已完成
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### 4.3 保洁员信息 DTO `CleanerDTO`

```java
// user-service 返回给 room-service
public class CleanerDTO {
    private Long id;
    private String realName;
    private String phone;
    private int currentTaskCount;   // 当前打扫中任务数
}
```

### 4.4 分页查询对象 `PageQuery`

```java
public class PageQuery {
    private int page = 1;
    private int size = 10;
}
```

---

## 五、Feign 接口声明

> common-service 中只声明接口，实现由被调用方提供。调用方直接注入即可使用。

### 5.1 RoomClient（order-service 调用 room-service）

```java
@FeignClient(name = "room-service", path = "/api/room")
public interface RoomClient {

    @GetMapping("/{id}")
    Result<RoomVO> getRoomById(@PathVariable Long id);

    @PutMapping("/{id}/status")
    Result<Void> changeStatus(@PathVariable Long id,
                              @RequestBody RoomStatusChangeDTO dto);
}
```

### 5.2 UserClient（room-service 调用 user-service）

```java
@FeignClient(name = "user-service", path = "/api/user")
public interface UserClient {

    @GetMapping("/cleaner/list")
    Result<List<CleanerDTO>> getCleanerList();

    @GetMapping("/customer/{id}")
    Result<CustomerVO> getCustomer(@PathVariable Long id);
}
```

---

## 六、全局异常处理

> 各服务引入 common-service 后自动生效。

### 6.1 业务异常

```java
public class BusinessException extends RuntimeException {
    private final int code;

    // 使用预设错误码
    public BusinessException(ErrorCode errorCode) { ... }
    // 自定义消息
    public BusinessException(int code, String msg) { ... }
}
```

### 6.2 全局拦截 `@RestControllerAdvice`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMsg());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(400, msg);
    }

    // 兜底异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未预期异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
```

### 6.3 业务校验断言 `Assert`

```java
// 替代冗长的 if → throw，简化代码
Assert.notNull(room, "房间不存在");                            // 抛 BusinessException(400)
Assert.isTrue(room.getStatus().canTransitionTo(target),       // 抛 BusinessException(400)
              "非法状态跳转: " + room.getStatus() + " → " + target);
Assert.hasPermission(userRole, UserRole.ADMIN, "仅管理员可操作"); // 抛 BusinessException(403)
```

---

## 七、工具类

| 类 | 用途 |
|----|------|
| `JwtUtil` | 生成 / 解析 / 校验 JWT Token |
| `SnowflakeIdUtil` | 雪花算法生成全局唯一 ID（订单号、支付流水号） |
| `DateUtil` | 日期格式化、日期范围计算（nights计算）、LocalDate 工具 |
| `StrUtil` | 字符串处理（脱敏手机号、身份证号等） |
| `JsonUtil` | JSON 序列化/反序列化（基于 FastJSON2） |
| `RedisUtil` | Redis 读写简化（Token 黑名单、缓存） |
| `OssUtil` | 阿里云 OSS 文件上传/删除（返回 HTTPS URL） |
| `UserContext` | 基于 ThreadLocal 的请求级用户上下文传递 |
| `SpringContextUtil` | 获取 Spring Bean（非容器管理类中使用） |

