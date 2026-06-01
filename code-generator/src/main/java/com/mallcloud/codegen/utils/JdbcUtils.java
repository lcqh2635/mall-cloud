package com.mallcloud.codegen.utils;

import com.mallcloud.codegen.model.entity.DatasourceEntity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * JDBC 工具类 —— 负责根据 Datasource 配置动态创建数据库连接池
 */
public class JdbcUtils {

    /**
     * 根据数据源配置创建一个 HikariCP 连接池
     * 注意：这里每次调用都会创建新的连接池，仅用于元数据解析等短期操作，用完需关闭
     */
    public static DataSource createDataSource(DatasourceEntity ds) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(ds.getDbType().getDriverClassName());
        config.setJdbcUrl(ds.getDbType().buildUrl(ds.getHost(), ds.getPort(), ds.getDbName()));
        config.setUsername(ds.getUsername());
        config.setPassword(ds.getPassword());
        // 连接池大小设为最小，因为只是临时获取元数据
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000); // 5秒超时
        config.setIdleTimeout(30000);
        config.setPoolName("meta-pool-" + ds.getId());
        return new HikariDataSource(config);
    }

    /**
     * 测试数据源连接是否可用
     * @return true 表示连接成功
     */
    public static boolean testConnection(DatasourceEntity ds) {
        try (HikariDataSource dataSource = (HikariDataSource) createDataSource(ds)) {
            // 尝试获取连接，成功则说明可用
            dataSource.getConnection().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}