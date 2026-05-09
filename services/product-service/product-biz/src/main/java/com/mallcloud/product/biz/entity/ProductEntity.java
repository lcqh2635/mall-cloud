package com.mallcloud.product.biz.entity;

import com.mallcloud.commons.mybatis.entity.BaseEntity;
import com.mybatisflex.annotation.*;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.core.mask.Masks;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * MyBatis-Flex 常用的注解参考示例
 * 在 MyBatis-Flex 中，@Table 主要是用于给 Entity 实体类添加标识，用于描述 实体类 和 数据库表 的关系，以及对实体类进行的一些 功能辅助
 */
@Getter
@Setter
@Table(value = "t_product", comment = "文章表")
public class ProductEntity extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    // 参看 MyBatis-Flex 官方文档 https://mybatis-flex.com/zh/core/column.html#ignore
    // 当我们为了业务需要，在 entity 类中添加了某个字段，但是数据库却不存在该列时，使用 @Column(ignore = true) 修饰。
    @Column(ignore = true)
    private String ignore;

    // 用于标识这个字段是否是大字段，比如存放文章的文章字段，在一般的场景中是没必要对这个字段进行查询的
    @Column(isLarge = true)
    private String content;
}

