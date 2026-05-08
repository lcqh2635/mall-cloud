package com.mallcloud.commons.job.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * XXL-JOB 配置属性类
 * 用于接收和绑定application.yml中以"xxl.job"为前缀的配置项
 */
@Getter
@Setter
@ConfigurationProperties(prefix = XxlJobProperties.PREFIX)
public class XxlJobProperties {

    /**
     * 配置属性前缀常量
     * 对应配置文件中的 xxl-job 前缀
     */
    public static final String PREFIX = "xxl-job";

    // 嵌套配置属性，注解参考 DynamicDataSourceProperties
    /**
     * XXL-JOB 管理端配置属性
     * 对应配置文件中的 xxl.job.admin.* 配置项
     */
    // 嵌套配置属性，注解参考 DynamicDataSourceProperties
    @NestedConfigurationProperty
    private XxlJobAdminProperties admin;

    /**
     * XXL-JOB 执行器配置属性
     * 对应配置文件中的 xxl.job.executor.* 配置项
     */
    @NestedConfigurationProperty
    private XxlJobExecutorProperties executor;

    /**
     * XXL-JOB 管理端配置内部类
     * 用于封装管理端相关配置参数
     */
    @Getter
    @Setter
    public static class XxlJobAdminProperties {
        /**
         * 管理端地址
         * 示例: <a href="http://localhost:8080/xxl-job-admin">...</a>
         */
        private String address;
    }

    /**
     * XXL-JOB 执行器配置内部类
     * 用于封装执行器相关配置参数
     */
    @Getter
    @Setter
    public static class XxlJobExecutorProperties {
        /**
         * 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
         */
        private String appname;
        /**
         * 服务注册地址,优先使用该配置作为注册地址 为空时使用内嵌服务 ”IP:PORT“ 作为注册地址 从而更灵活的支持容器类型执行器动态IP和动态映射端口问题
         */
        private String address;
        /**
         * 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9099，单机部署多个执行器时，注意要配置不同执行器端口；
         */
        private int port = 9999;
        /**
         * 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP ，该IP不会绑定Host仅作为通讯实用；地址信息用于 "执行器注册" 和
         * "调度中心请求并触发任务"
         */
        private String ip;
        /**
         * 执行器通讯TOKEN [必填]：从配置文件中取不到值时使用默认值；
         */
        private String accessToken = "default_token";
        /**
         * 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
         */
        private String logPath = "logs/applogs/xxl-job/jobhandler";
        /**
         * 执行器日志保存天数 [选填] ：值大于3时生效，启用执行器Log文件定期清理功能，否则不生效；
         */
        private Integer logRetentionDays = 30;
    }
}
