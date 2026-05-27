-- 1. 创建数据源配置表
CREATE TABLE codegen_datasource
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE, -- 数据源名称（如：订单库、用户库）
    db_url       VARCHAR(500) NOT NULL,        -- JDBC URL
    db_username  VARCHAR(100) NOT NULL,        -- 数据库账号
    db_password  VARCHAR(255) NOT NULL,        -- 数据库密码（建议加密存储，此处演示使用 Base64）
    base_package VARCHAR(200) NOT NULL,        -- 基础包名（如 com.company.order）
    author       VARCHAR(50) DEFAULT 'System', -- 生成代码的作者名
    status       SMALLINT    DEFAULT 1,        -- 状态：1-启用，0-禁用
    create_time  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE codegen_datasource IS '代码生成器-数据源配置表';
COMMENT
ON COLUMN codegen_datasource.status IS '状态：1-启用，0-禁用';

-- 2. 创建代码模板配置表
CREATE TABLE codegen_template
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,   -- 模板名称（如：标准Entity模板、包含Swagger的Controller模板）
    template_type VARCHAR(50)  NOT NULL,   -- 模板类型：entity, mapper, service, service_impl, controller
    content       TEXT         NOT NULL,   -- 模板内容（Velocity 语法字符串）
    is_default    BOOLEAN   DEFAULT FALSE, -- 是否为该类型的默认模板
    status        SMALLINT  DEFAULT 1,     -- 状态：1-启用，0-禁用
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE codegen_template IS '代码生成器-代码模板配置表';
COMMENT
ON COLUMN codegen_template.template_type IS '模板类型：entity, mapper, service, service_impl, controller';
COMMENT
ON COLUMN codegen_template.content IS 'Velocity 模板引擎语法内容';

-- 插入一些初始的默认模板数据（以 Entity 为例）
INSERT INTO codegen_template (name, template_type, content, is_default)
VALUES ('标准 Entity 模板', 'entity',
        'package ${entityPackage};

        import lombok.Data;
        import com.mybatisflex.annotation.Table;
        import com.mybatisflex.annotation.Id;
        import com.mybatisflex.annotation.KeyType;

        /**
         * ${table.comment}
         *
         * @author ${author}
         */
        @Data
        @Table("${table.name}")
        public class ${entityName} {

            #foreach($column in $columns)
            /**
             * ${column.comment}
             */
            #if($column.isPrimaryKey)
            @Id(keyType = KeyType.Auto)
            #end
            private ${column.javaType} ${column.javaField};

            #end
        }', TRUE);