package ${package.Mapper};

import ${package.Entity}.${entity};
import ${superMapperClassPackage};

<#list importPackages as pkg>
import ${pkg};
</#list>
<#if mapperAnnotationClass??>
import $
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;{mapperAnnotationClass.name};
</#if>

/**
 * MyBatis-Plus 全局配置类
 * 功能：
 *   - 配置分页插件（Pagination）
 *   - 配置乐观锁插件（OptimisticLocker）
 *   - 配置逻辑删除插件（LogicalDelete）
 *   - 配置多租户插件（TenantLine）
 *   - 配置 SQL 日志拦截器（自动注入 traceId）
 *   - 配置自动填充处理器（createTime/updateTime）
 * <p>
 * 注意：
 *   - 此类会被所有服务自动加载（通过 @ComponentScan）
 *
 * @author ${author}
 * @since ${date}
 */
@Configuration
@MapperScan("com.urbane.*")
public class MybatisPlusConfig {

    @Resource
    private MybatisPlusTenantHandler mybatisPlusTenantHandler;

    /**
     * 新版分页插件设置，可根据需求选择添加
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 插件主体，封装其他场景插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 自动分页插件，以 MYSQL 为数据库
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // SQL 性能规范插件
        interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 多租户插件，参考 https://baomidou.com/plugins/tenant/
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(mybatisPlusTenantHandler);
        interceptor.addInnerInterceptor(tenantInterceptor);
        return interceptor;
    }
}

// 此处内容参考 Mybatis-Plus 官网，插件主体 https://baomidou.com/plugins/

