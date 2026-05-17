# 在Store中直接引入Service，避免组件直接调用API

这个设计原则的核心是 **"关注点分离"** 和 **"单一职责"**，目的是让代码更易维护、降低耦合度。我来通过具体示例解释：

---

### **❌ 不推荐的做法（组件直接调用API）**
```vue
<!-- 组件中直接调用API（问题示例） -->
<script setup>
import { userApi } from '@/services/modules/user' // 直接引入API
import { useUserStore } from '@/stores/modules/user'

const store = useUserStore()

// 组件中直接调用API（耦合度过高）
const handleLogin = async () => {
  try {
    const res = await userApi.login({ username: 'admin', password: '123' })
    store.setUser(res.data) // 手动更新Store
  } catch (err) {
    console.error(err)
  }
}
</script>
```

**存在的问题**：
1. **业务逻辑分散**：相同的API调用可能在不同组件重复出现
2. **难以维护**：如果API路径或参数变化，需要修改所有相关组件
3. **状态管理混乱**：需要手动同步Store和API响应

---

### **✅ 推荐的做法（Store调用Service）**
#### 1. 首先在Service层封装API
```typescript
// services/modules/user/api.ts
export const userApi = {
  login: (params: LoginParams) => axios.post('/auth/login', params),
  getProfile: () => axios.get('/user/profile')
}
```

#### 2. 在Store中集中管理业务逻辑
```typescript
// stores/modules/user.store.ts
import { userApi } from '@/services/modules/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as UserInfo | null,
    token: ''
  }),
  actions: {
    // 封装登录逻辑（集中处理错误、状态更新）
    async login(credentials: LoginParams) {
      try {
        const { data } = await userApi.login(credentials)
        this.token = data.token
        this.user = data.user
        localStorage.setItem('token', data.token)
      } catch (error) {
        // 统一错误处理
        throw new Error('Login failed')
      }
    }
  }
})
```

#### 3. 组件只与Store交互
```vue
<!-- 组件调用Store Action -->
<script setup>
const store = useUserStore()

const handleLogin = () => {
  // 组件不关心API细节，只触发业务动作
  store.login({ username: 'admin', password: '123' })
}
</script>
```

---

### **关键设计思想**
1. **分层架构**：
    - **Service层**：只做纯粹的API通信（axios封装）
    - **Store层**：组合多个API调用，处理业务逻辑（如登录后更新token+用户信息）
    - **组件层**：仅触发行为，不处理具体逻辑

2. **优势**：
    - **复用性**：多个组件共享同一套业务逻辑
    - **可测试性**：可以单独测试Store的登录逻辑
    - **维护性**：API变更只需修改Store，不影响组件
    - **状态一致性**：保证API响应和Store状态自动同步

3. **典型场景示例**：
   ```typescript
   // 复杂业务逻辑示例（Store中组合多个API）
   async function checkout() {
     // 1. 创建订单
     const order = await orderApi.create() 
     // 2. 扣减库存
     await inventoryApi.deduct(order.items)
     // 3. 更新用户积分
     this.user.points += order.points
   }
   ```

---

### **何时可以打破这个原则？**
在简单场景下允许组件直接调用Service：
1. **完全独立的API**：如获取天气数据，不需要进入Store
2. **一次性请求**：如页面初始化时的统计数据获取
3. **原型开发阶段**：快速验证时可暂时简化

但核心业务逻辑（用户、订单、支付等）强烈建议通过Store管理。