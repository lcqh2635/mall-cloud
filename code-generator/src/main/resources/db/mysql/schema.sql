-- ===================================================================
-- 代码生成器管理数据库 DDL（MySQL 版本）
-- 使用说明：创建 database 后直接执行本脚本即可
-- ===================================================================

CREATE
DATABASE IF NOT EXISTS `code_generator`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE
`code_generator`;

-- -------------------------------------------------------------------
-- 1. 项目表：存储用户创建的每个代码生成项目
-- -------------------------------------------------------------------
CREATE TABLE `gen_project`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name`          VARCHAR(100) NOT NULL COMMENT '项目名称',
    `package_name`  VARCHAR(200) NOT NULL COMMENT '基础包名，如 com.example.demo',
    `base_path`     VARCHAR(200)          DEFAULT 'src/main/java' COMMENT '生成代码基础路径',
    `author`        VARCHAR(50)           DEFAULT '' COMMENT '作者名称',
    `version`       VARCHAR(20)           DEFAULT '1.0.0' COMMENT '项目版本号',
    `description`   VARCHAR(500)          DEFAULT '' COMMENT '项目描述',
    `remove_prefix` VARCHAR(50)           DEFAULT '' COMMENT '表前缀去除（如 t_，多个用逗号分隔）',
    `extra_config`  JSON COMMENT '扩展配置（如自定义变量等，JSON格式）',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX           `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

-- -------------------------------------------------------------------
-- 2. 数据源连接表：存储数据库连接信息（支持真实连接和虚拟数据源）
-- -------------------------------------------------------------------
CREATE TABLE `gen_datasource`
(
    `id`                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name`                VARCHAR(100) NOT NULL COMMENT '连接名称（用户自定义）',
    `db_type`             VARCHAR(30)           DEFAULT '' COMMENT '数据库类型：MYSQL、POSTGRESQL、ORACLE等',
    `host`                VARCHAR(100)          DEFAULT '' COMMENT '主机地址',
    `port`                INT                   DEFAULT 3306 COMMENT '端口号',
    `username`            VARCHAR(100)          DEFAULT '' COMMENT '数据库用户名',
    `password`            VARCHAR(255)          DEFAULT '' COMMENT '密码（建议加密存储，此处简化）',
    `db_name`             VARCHAR(100)          DEFAULT '' COMMENT '数据库名/SID',
    `type`                VARCHAR(20)  NOT NULL DEFAULT 'JDBC' COMMENT '数据源类别：JDBC（真实连接）| VIRTUAL（手动设计）',
    `virtual_tables_json` JSON COMMENT '当类型为VIRTUAL时，存储手动设计的表结构（JSON数组）',
    `alive`               TINYINT(1) DEFAULT 0 COMMENT '连接状态：1-正常，0-失败（由定时任务更新）',
    `last_check_time`     DATETIME COMMENT '最后一次连接检测时间',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX                 `idx_type` (`type`),
    INDEX                 `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- -------------------------------------------------------------------
-- 3. 模板组表：存储代码模板的分组信息
-- -------------------------------------------------------------------
CREATE TABLE `gen_template_group`
(
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name`         VARCHAR(100) NOT NULL COMMENT '模板组名称（唯一，如 springboot-mybatisplus）',
    `display_name` VARCHAR(200) NOT NULL COMMENT '显示名称（如 SpringBoot+MyBatisPlus全套）',
    `description`  VARCHAR(500)          DEFAULT '' COMMENT '模板组描述',
    `framework`    VARCHAR(50)           DEFAULT '' COMMENT '适用框架（如 SpringBoot, SpringCloud, Vue3）',
    `is_builtin`   TINYINT(1) DEFAULT 0 COMMENT '是否内置模板（内置不可删除）',
    `is_public`    TINYINT(1) DEFAULT 0 COMMENT '是否公开（公开后其他用户可使用）',
    `creator`      VARCHAR(100)          DEFAULT 'system' COMMENT '创建者',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板组表';

-- -------------------------------------------------------------------
-- 4. 模板文件表：存储模板组中每个具体的模板文件
-- -------------------------------------------------------------------
CREATE TABLE `gen_template_file`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `group_id`      BIGINT       NOT NULL COMMENT '所属模板组ID',
    `file_name`     VARCHAR(100) NOT NULL COMMENT '文件名（仅展示用，如 Entity.java）',
    `template_path` VARCHAR(300) NOT NULL COMMENT '模板文件在classpath下的相对路径（如 springboot-mybatisplus/Entity.java.ftl）',
    `output_path`   VARCHAR(300) NOT NULL COMMENT '生成后文件的输出路径模板（支持占位符，如 {packagePath}/entity/{className}.java）',
    `category`      VARCHAR(50)           DEFAULT '' COMMENT '文件分类：entity、mapper、service、controller、vue等',
    `sort_order`    INT                   DEFAULT 0 COMMENT '排序序号',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX           `idx_group_id` (`group_id`),
    CONSTRAINT `fk_template_file_group` FOREIGN KEY (`group_id`) REFERENCES `gen_template_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板文件表';

-- -------------------------------------------------------------------
-- 5. 生成任务记录表：记录每次代码生成操作的详情
-- -------------------------------------------------------------------
CREATE TABLE `gen_task`
(
    `id`                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `project_id`        BIGINT COMMENT '关联项目ID（可为空）',
    `datasource_id`     BIGINT COMMENT '使用的数据源ID',
    `template_group_id` BIGINT COMMENT '使用的模板组ID',
    `table_names`       JSON        NOT NULL COMMENT '生成的表名列表（JSON数组）',
    `custom_vars`       JSON COMMENT '自定义变量（JSON对象）',
    `strategy_json`     JSON COMMENT '生成策略配置（如覆盖模式、忽略字段等）',
    `status`            VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '任务状态：SUCCESS、FAIL、PARTIAL',
    `error_message`     TEXT COMMENT '错误信息（失败时记录）',
    `file_count`        INT                  DEFAULT 0 COMMENT '生成的文件总数',
    `output_zip_path`   VARCHAR(500) COMMENT '生成的ZIP文件存储路径（服务器本地）',
    `operator`          VARCHAR(100)         DEFAULT 'anonymous' COMMENT '操作人',
    `create_time`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX               `idx_project_id` (`project_id`),
    INDEX               `idx_create_time` (`create_time`),
    CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `gen_project` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_task_datasource` FOREIGN KEY (`datasource_id`) REFERENCES `gen_datasource` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_task_template_group` FOREIGN KEY (`template_group_id`) REFERENCES `gen_template_group` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成任务记录表';

-- -------------------------------------------------------------------
-- 6. 全局参数表：存储系统级别的默认变量（如作者、日期格式等）
-- -------------------------------------------------------------------
CREATE TABLE `gen_global_param`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `param_key`   VARCHAR(100) NOT NULL COMMENT '参数键（如 author、dateFormat）',
    `param_value` VARCHAR(500) NOT NULL COMMENT '参数值',
    `description` VARCHAR(200)          DEFAULT '' COMMENT '参数说明',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX `uk_param_key` (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局参数表';

-- -------------------------------------------------------------------
-- 7. 类型映射表：自定义数据库类型到Java类型的转换规则
-- -------------------------------------------------------------------
CREATE TABLE `gen_type_mapping`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `db_type`     VARCHAR(50)  NOT NULL COMMENT '数据库字段类型（如 VARCHAR, INT）',
    `java_type`   VARCHAR(100) NOT NULL COMMENT '映射的Java类型全名（如 java.lang.String）',
    `description` VARCHAR(200)          DEFAULT '' COMMENT '备注说明',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX `uk_db_type` (`db_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库类型到Java类型映射表';