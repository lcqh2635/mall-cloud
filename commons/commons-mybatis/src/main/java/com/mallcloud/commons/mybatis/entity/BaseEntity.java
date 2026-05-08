package com.mallcloud.commons.mybatis.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有业务实体类的基类 —— 封装通用字段
 * <p>
 * 实际开发中，建议所有实体类继承此类，统一管理：
 * - 主键策略
 * - 创建/更新时间自动填充
 * - 逻辑删除字段
 * - 乐观锁版本控制
 * - 租户ID（如需多租户）
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false) // 重写 equals 和 hashCode，避免继承问题
@Accessors(chain = true) // 支持链式调用：new User().setName("张三").setAge(20)
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用雪花算法（分布式唯一ID），避免自增主键暴露业务量或分库分表问题
     * IdType.ASSIGN_ID：MP 3.3.0+ 默认策略，整合雪花算法，Long/Integer/String 均支持
     */
    private Long id;

    /**
     * 逻辑删除字段（0=未删除，1=已删除）
     * 配合 @TableLogic 注解，MP 查询时自动追加 WHERE deleted = 0
     * 删除操作会转为 UPDATE SET deleted = 1
     * 全局配置可设置默认值，这里显式标注便于阅读
     */
    private Integer deleted = 0; // 默认未删除

    /**
     * 乐观锁版本号
     * 更新时自动比对版本，防止并发覆盖
     * 更新成功后版本号 +1
     * 需配合插件：OptimisticLockerInnerInterceptor
     */
    private Integer version = 1; // 默认版本为1

    /**
     * 创建时间
     * 自动填充：插入时赋值
     * FieldFill.INSERT：仅插入时填充
     */
    private LocalDateTime createTime;

    /**
     * 【可选】创建人ID（记录谁创建的）
     * 可配合 Spring Security 或自定义上下文获取当前用户ID
     */
    private Long createUserId;

    /**
     * 更新时间
     * 自动填充：插入和更新时都赋值
     * FieldFill.INSERT_UPDATE：插入和更新时填充
     */
    private LocalDateTime updateTime;

    /**
     * 【可选】更新人ID
     */
    private Long updateUserId;

    /**
     * 【可选】租户ID（适用于SaaS多租户系统）
     * 配合 MyBatis-Plus 多租户插件 TenantLineInnerInterceptor 使用
     * 每次查询/更新自动追加 AND tenant_id = ?
     */
    private Long tenantId;
}