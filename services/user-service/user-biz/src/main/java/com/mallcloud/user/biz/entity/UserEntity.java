package com.mallcloud.user.biz.entity;

import com.mallcloud.commons.mybatis.entity.BaseEntity;
import com.mybatisflex.annotation.*;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.core.mask.Masks;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MyBatis-Flex 常用的注解参考示例
 * 在 MyBatis-Flex 中，@Table 主要是用于给 Entity 实体类添加标识，用于描述 实体类 和 数据库表 的关系，以及对实体类进行的一些 功能辅助
 */
@Table(value = "tb_article", comment = "文章表")
public class UserEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // id 为自增主键
    // 在 Entity 类中，MyBatis-Flex 是使用 @Id 注解来标识主键
    // 参考 https://mybatis-flex.com/zh/core/id.html#%E5%A4%9A%E4%B8%BB%E9%94%AE%E3%80%81%E5%A4%8D%E5%90%88%E4%B8%BB%E9%94%AE
    @Id(keyType = KeyType.Auto)
    private Long id;

    // uuid：通过 UUIDKeyGenerator 生成 UUID 作为数据库主键
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    // flexId：独创的 FlexID 算法生成数据库主键
//    @Id(keyType = KeyType.Generator, value= KeyGenerators.flexId)
    // snowFlakeId：通过雪花算法（SnowFlakeIDKeyGenerator）生成数据库主键
//    @Id(keyType=KeyType.Generator, value=KeyGenerators.snowFlakeId)
    private String otherId;

    // MyBatis-Flex 提供了 @ColumnMask() 注解，以及内置的9种脱敏规则，帮助开发者方便的进行数据脱敏
    // 用户真实姓名脱敏
    @ColumnMask(Masks.CHINESE_NAME)
    private String userName;

    // 密码脱敏
    @ColumnMask(Masks.PASSWORD)
    private String password;

    // 邮箱脱敏
    @ColumnMask(Masks.EMAIL)
    private String email;

    // 手机号码脱敏
    @ColumnMask(Masks.MOBILE)
    private String mobile;

    // 用户家庭住址脱敏
    @ColumnMask(Masks.ADDRESS)
    private String address;

    // 身份证脱敏
    @ColumnMask(Masks.ID_CARD_NUMBER)
    private String idCard;

    // 银行卡脱敏
    @ColumnMask(Masks.BANK_CARD_NUMBER)
    private String bankCard;

    // 驾照号脱敏
    @ColumnMask(Masks.CAR_LICENSE)
    private String carLicense;

    // onInsertValue 当数据被插入时，设置的默认值
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    // onUpdateValue 当数据被更新时，设置的默认值
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // isLogicDelete 逻辑删除字段
    @Column(isLogicDelete = true)
    private Boolean isDelete;

    // version 乐观锁字段
    @Column(version = true)
    private Long version;

    // 参看 MyBatis-Flex 官方文档 https://mybatis-flex.com/zh/core/column.html#ignore
    // 当我们为了业务需要，在 entity 类中添加了某个字段，但是数据库却不存在该列时，使用 @Column(ignore = true) 修饰。
    @Column(ignore = true)
    private String ignore;

    // 用于标识这个字段是否是大字段，比如存放文章的文章字段，在一般的场景中是没必要对这个字段进行查询的
    @Column(isLarge = true)
    private String content;
}
