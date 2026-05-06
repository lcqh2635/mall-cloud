太好了！下面我将为你提供一个 **完整的 Spring Boot + MyBatis 游标分页（Cursor-based Pagination）实现**，适用于真实项目开发。

> ✅ 为什么推荐游标分页？  
> 传统 `LIMIT + OFFSET` 在深度分页时性能极差（如 `OFFSET 10000`），而**游标分页基于索引条件查询，性能稳定**，适合大数据量场景（如订单列表、消息流、日志等）。

---

## 🧩 一、项目结构概览

```
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── controller/OrderController.java
│   │       ├── service/OrderService.java
│   │       ├── mapper/OrderMapper.java
│   │       ├── model/Order.java
│   │       └── dto/OrderDTO.java
│   │       └── request/CursorPageRequest.java
│   │       └── response/CursorPageResult.java
│   └── resources/
│       └── mapper/OrderMapper.xml
```

---

## ✅ 二、数据库准备（PostgreSQL）

```sql
-- 创建订单表
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    user_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 为游标分页创建复合索引（状态 + 时间倒序）
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);
```

> ✅ **索引说明**：按 `status` 等值查询 + `created_at` 范围查询，适合游标分页。

---

## ✅ 三、Java 模型类

### 1. `Order.java`（实体类）

```java
// com/example/demo/model/Order.java
package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Order {
    private Long id;
    private String orderNo;
    private UUID userId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    // getter 和 setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

---

### 2. `OrderDTO.java`（接口返回 DTO）

```java
// com/example/demo/dto/OrderDTO.java
package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDTO {
    private Long id;
    private String orderNo;
    private UUID userId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    // 构造函数
    public OrderDTO(Order order) {
        this.id = order.getId();
        this.orderNo = order.getOrderNo();
        this.userId = order.getUserId();
        this.amount = order.getAmount();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }

    // getter 和 setter（略）
    // 建议使用 Lombok @Data 简化
}
```

---

### 3. `CursorPageRequest.java`（分页请求参数）

```java
// com/example/demo/request/CursorPageRequest.java
package com.example.demo.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class CursorPageRequest {
    @NotBlank(message = "状态不能为空")
    private String status;           // 查询状态，如 "paid"

    private String cursor;           // 游标：上一页最后一条的 created_at ISO 格式
    @Min(1)
    private Integer limit = 10;      // 每页数量，默认 10

    // getter 和 setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCursor() { return cursor; }
    public void setCursor(String cursor) { this.cursor = cursor; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
```

---

### 4. `CursorPageResult.java`（分页返回结果）

```java
// com/example/demo/response/CursorPageResult.java
package com.example.demo.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CursorPageResult<T> {
    private List<T> data;
    private String nextCursor;  // 下一页游标（ISO 时间格式）
    private boolean hasNext;    // 是否还有下一页

    public CursorPageResult(List<T> data, LocalDateTime lastTime, int limit) {
        this.data = data;
        this.hasNext = data != null && data.size() == limit; // 如果数量等于 limit，说明还有下一页
        if (hasNext && lastTime != null) {
            this.nextCursor = lastTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            this.nextCursor = null;
        }
    }

    // getter 和 setter
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}
```

---

## ✅ 四、MyBatis Mapper

### 1. `OrderMapper.java`

```java
// com/example/demo/mapper/OrderMapper.java
package com.example.demo.mapper;

import com.example.demo.model.Order;
import com.example.demo.request.CursorPageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 游标分页查询订单
     * 条件：status = ? AND created_at < ?
     * 按 created_at 倒序排列
     */
    List<Order> selectByCursor(
        @Param("status") String status,
        @Param("cursor") LocalDateTime cursor,
        @Param("limit") Integer limit
    );
}
```

---

### 2. `OrderMapper.xml`

```xml
<!-- src/main/resources/mapper/OrderMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.demo.mapper.OrderMapper">

    <select id="selectByCursor" resultType="com.example.demo.model.Order">
        SELECT
            id,
            order_no,
            user_id,
            amount,
            status,
            created_at
        FROM orders
        WHERE status = #{status}
          <!-- 如果提供了游标，则只查比它早的数据 -->
          <if test="cursor != null">
            AND created_at &lt; #{cursor}
          </if>
        ORDER BY created_at DESC
        LIMIT #{limit}
    </select>

</mapper>
```

> 🔍 注意：`&lt;` 是 `<` 的 XML 转义。

---

## ✅ 五、Service 层

```java
// com/example/demo/service/OrderService.java
package com.example.demo.service;

import com.example.demo.dto.OrderDTO;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.model.Order;
import com.example.demo.request.CursorPageRequest;
import com.example.demo.response.CursorPageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public CursorPageResult<OrderDTO> getOrdersByCursor(CursorPageRequest request) {
        // 1. 解析游标（ISO 时间字符串 → LocalDateTime）
        LocalDateTime cursor = null;
        if (request.getCursor() != null && !request.getCursor().isEmpty()) {
            cursor = LocalDateTime.parse(request.getCursor(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        // 2. 查询数据（最多 limit + 1 条，判断是否有下一页）
        List<Order> orders = orderMapper.selectByCursor(
            request.getStatus(),
            cursor,
            request.getLimit() + 1  // 多查 1 条，判断是否还有下一页
        );

        // 3. 判断是否还有下一页
        boolean hasNext = orders.size() > request.getLimit();
        // 截取到 limit 条
        List<Order> resultOrders = hasNext ? orders.subList(0, request.getLimit()) : orders;

        // 4. 转换为 DTO
        List<OrderDTO> dtoList = resultOrders.stream()
            .map(OrderDTO::new)
            .collect(Collectors.toList());

        // 5. 获取最后一条的时间作为下一页游标
        LocalDateTime lastTime = resultOrders.isEmpty() ? null : resultOrders.get(resultOrders.size() - 1).getCreatedAt();

        // 6. 返回分页结果
        return new CursorPageResult<>(dtoList, lastTime, request.getLimit());
    }
}
```

---

## ✅ 六、Controller 层

```java
// com/example/demo/controller/OrderController.java
package com.example.demo.controller;

import com.example.demo.request.CursorPageRequest;
import com.example.demo.response.CursorPageResult;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/cursor")
    public CursorPageResult<?> getOrdersByCursor(@Valid CursorPageRequest request) {
        return orderService.getOrdersByCursor(request);
    }
}
```

---

## ✅ 七、使用示例（API 调用）

### 第一页请求：
```http
GET /api/orders/cursor?status=paid&limit=5
```

### 返回：
```json
{
  "data": [
    { "id": 5, "orderNo": "ORD005", "createdAt": "2024-06-01T10:00:00" },
    { "id": 4, "orderNo": "ORD004", "createdAt": "2024-06-01T09:30:00" }
  ],
  "nextCursor": "2024-06-01T09:30:00",
  "hasNext": true
}
```

### 第二页请求（带游标）：
```http
GET /api/orders/cursor?status=paid&limit=5&cursor=2024-06-01T09:30:00
```

---

## ✅ 八、优点总结

| 优点 | 说明 |
|------|------|
| ⚡ 高性能 | 基于索引范围查询，避免 `OFFSET` 扫描 |
| 📈 可扩展 | 支持百万级数据分页 |
| 🔄 一致性好 | 不受中间数据插入影响（相比 `OFFSET`） |
| 🔍 易集成 | 与 MyBatis、Spring Boot 完美配合 |

---

## 🎁 附加建议

1. **前端存储 `nextCursor`**，点击“加载更多”时传回。
2. **游标字段建议用 `created_at` 或 `id`（时间或自增）**
3. **复合索引必须包含游标字段**
4. **可扩展支持多字段游标（如 `status + created_at`）**

---

如果你需要，我可以为你生成：
- 一个 **Postman 测试集合**
- 一个 **JPA 版本的游标分页实现**
- 一个 **前端 Vue 分页组件示例**

欢迎继续提问！🚀