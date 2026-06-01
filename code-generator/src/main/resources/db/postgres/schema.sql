-- ===================================================================
-- 代码生成器管理数据库 DDL（PostgresSQL 版本）
-- 使用说明：连接到数据库后直接执行本脚本
-- ===================================================================

-- 1. 项目信息表
CREATE TABLE gen_project
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    package_name  VARCHAR(200) NOT NULL,
    base_path     VARCHAR(200)          DEFAULT 'src/main/java',
    author        VARCHAR(50)           DEFAULT '',
    version       VARCHAR(20)           DEFAULT '1.0.0',
    description   VARCHAR(500)          DEFAULT '',
    remove_prefix VARCHAR(50)           DEFAULT '',
    extra_config  JSONB                 DEFAULT '{}'::jsonb,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT
ON TABLE gen_project IS '项目信息表';
COMMENT
ON COLUMN gen_project.name IS '项目名称';
COMMENT
ON COLUMN gen_project.package_name IS '基础包名，如 com.example.demo';
COMMENT
ON COLUMN gen_project.base_path IS '生成代码基础路径';
COMMENT
ON COLUMN gen_project.author IS '作者名称';
COMMENT
ON COLUMN gen_project.version IS '项目版本号';
COMMENT
ON COLUMN gen_project.description IS '项目描述';
COMMENT
ON COLUMN gen_project.remove_prefix IS '表前缀去除（如 t_）';
COMMENT
ON COLUMN gen_project.extra_config IS '扩展配置（JSON格式）';
COMMENT
ON COLUMN gen_project.create_time IS '创建时间';
COMMENT
ON COLUMN gen_project.update_time IS '更新时间';
CREATE INDEX idx_gen_project_name ON gen_project (name);

-- 2. 数据源配置表
CREATE TABLE gen_datasource
(
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    db_type             VARCHAR(30)           DEFAULT '',
    host                VARCHAR(100)          DEFAULT '',
    port                INT                   DEFAULT 3306,
    username            VARCHAR(100)          DEFAULT '',
    password            VARCHAR(255)          DEFAULT '',
    db_name             VARCHAR(100)          DEFAULT '',
    type                VARCHAR(20)  NOT NULL DEFAULT 'JDBC',
    virtual_tables_json JSONB,
    alive               BOOLEAN               DEFAULT FALSE,
    last_check_time     TIMESTAMP,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT
ON TABLE gen_datasource IS '数据源配置表';
COMMENT
ON COLUMN gen_datasource.name IS '连接名称（用户自定义）';
COMMENT
ON COLUMN gen_datasource.db_type IS '数据库类型：MYSQL、POSTGRESQL等';
COMMENT
ON COLUMN gen_datasource.host IS '主机地址';
COMMENT
ON COLUMN gen_datasource.port IS '端口号';
COMMENT
ON COLUMN gen_datasource.username IS '数据库用户名';
COMMENT
ON COLUMN gen_datasource.password IS '密码';
COMMENT
ON COLUMN gen_datasource.db_name IS '数据库名/SID';
COMMENT
ON COLUMN gen_datasource.type IS '数据源类别：JDBC / VIRTUAL';
COMMENT
ON COLUMN gen_datasource.virtual_tables_json IS '虚拟表结构JSON（当type=VIRTUAL时）';
COMMENT
ON COLUMN gen_datasource.alive IS '连接状态：true正常 false失败';
COMMENT
ON COLUMN gen_datasource.last_check_time IS '最后检测时间';
COMMENT
ON COLUMN gen_datasource.create_time IS '创建时间';
COMMENT
ON COLUMN gen_datasource.update_time IS '更新时间';
CREATE INDEX idx_gen_ds_type ON gen_datasource (type);
CREATE INDEX idx_gen_ds_name ON gen_datasource (name);

-- 3. 模板组表
CREATE TABLE gen_template_group
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description  VARCHAR(500)          DEFAULT '',
    framework    VARCHAR(50)           DEFAULT '',
    is_builtin   BOOLEAN               DEFAULT FALSE,
    is_public    BOOLEAN               DEFAULT FALSE,
    creator      VARCHAR(100)          DEFAULT 'system',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tpl_group_name UNIQUE (name)
);
COMMENT
ON TABLE gen_template_group IS '模板组表';
COMMENT
ON COLUMN gen_template_group.name IS '模板组唯一名称';
COMMENT
ON COLUMN gen_template_group.display_name IS '显示名称';
COMMENT
ON COLUMN gen_template_group.description IS '描述';
COMMENT
ON COLUMN gen_template_group.framework IS '适用框架';
COMMENT
ON COLUMN gen_template_group.is_builtin IS '是否内置模板';
COMMENT
ON COLUMN gen_template_group.is_public IS '是否公开';
COMMENT
ON COLUMN gen_template_group.creator IS '创建者';
COMMENT
ON COLUMN gen_template_group.create_time IS '创建时间';
COMMENT
ON COLUMN gen_template_group.update_time IS '更新时间';

-- 4. 模板文件表
CREATE TABLE gen_template_file
(
    id            BIGSERIAL PRIMARY KEY,
    group_id      BIGINT       NOT NULL,
    file_name     VARCHAR(100) NOT NULL,
    template_path VARCHAR(300) NOT NULL,
    output_path   VARCHAR(300) NOT NULL,
    category      VARCHAR(50)           DEFAULT '',
    sort_order    INT                   DEFAULT 0,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_template_file_group FOREIGN KEY (group_id) REFERENCES gen_template_group (id) ON DELETE CASCADE
);
COMMENT
ON TABLE gen_template_file IS '模板文件表';
COMMENT
ON COLUMN gen_template_file.group_id IS '所属模板组ID';
COMMENT
ON COLUMN gen_template_file.file_name IS '文件名（展示用）';
COMMENT
ON COLUMN gen_template_file.template_path IS '模板文件路径（classpath相对路径）';
COMMENT
ON COLUMN gen_template_file.output_path IS '输出路径模板（含占位符）';
COMMENT
ON COLUMN gen_template_file.category IS '文件分类';
COMMENT
ON COLUMN gen_template_file.sort_order IS '排序序号';
COMMENT
ON COLUMN gen_template_file.create_time IS '创建时间';
CREATE INDEX idx_gen_tpl_file_group ON gen_template_file (group_id);

-- 5. 生成任务记录表
CREATE TABLE gen_task
(
    id                BIGSERIAL PRIMARY KEY,
    project_id        BIGINT,
    datasource_id     BIGINT,
    template_group_id BIGINT,
    table_names       JSONB       NOT NULL,
    custom_vars       JSONB,
    strategy_json     JSONB,
    status            VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message     TEXT,
    file_count        INT                  DEFAULT 0,
    output_zip_path   VARCHAR(500),
    operator          VARCHAR(100)         DEFAULT 'anonymous',
    create_time       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES gen_project (id) ON DELETE SET NULL,
    CONSTRAINT fk_task_datasource FOREIGN KEY (datasource_id) REFERENCES gen_datasource (id) ON DELETE SET NULL,
    CONSTRAINT fk_task_template_group FOREIGN KEY (template_group_id) REFERENCES gen_template_group (id) ON DELETE SET NULL
);
COMMENT
ON TABLE gen_task IS '代码生成任务记录表';
COMMENT
ON COLUMN gen_task.project_id IS '关联项目ID';
COMMENT
ON COLUMN gen_task.datasource_id IS '数据源ID';
COMMENT
ON COLUMN gen_task.template_group_id IS '模板组ID';
COMMENT
ON COLUMN gen_task.table_names IS '生成的表名列表（JSON数组）';
COMMENT
ON COLUMN gen_task.custom_vars IS '自定义变量（JSON）';
COMMENT
ON COLUMN gen_task.strategy_json IS '生成策略配置（JSON）';
COMMENT
ON COLUMN gen_task.status IS '任务状态：SUCCESS/FAIL/PARTIAL';
COMMENT
ON COLUMN gen_task.error_message IS '错误信息';
COMMENT
ON COLUMN gen_task.file_count IS '生成文件总数';
COMMENT
ON COLUMN gen_task.output_zip_path IS '生成的ZIP路径';
COMMENT
ON COLUMN gen_task.operator IS '操作人';
COMMENT
ON COLUMN gen_task.create_time IS '创建时间';
CREATE INDEX idx_gen_task_project ON gen_task (project_id);
CREATE INDEX idx_gen_task_time ON gen_task (create_time);

-- 6. 全局参数表
CREATE TABLE gen_global_param
(
    id          BIGSERIAL PRIMARY KEY,
    param_key   VARCHAR(100) NOT NULL,
    param_value VARCHAR(500) NOT NULL,
    description VARCHAR(200)          DEFAULT '',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_global_param_key UNIQUE (param_key)
);
COMMENT
ON TABLE gen_global_param IS '全局参数表';
COMMENT
ON COLUMN gen_global_param.param_key IS '参数键（如 author）';
COMMENT
ON COLUMN gen_global_param.param_value IS '参数值';
COMMENT
ON COLUMN gen_global_param.description IS '参数说明';
COMMENT
ON COLUMN gen_global_param.create_time IS '创建时间';
COMMENT
ON COLUMN gen_global_param.update_time IS '更新时间';

-- 7. 类型映射表
CREATE TABLE gen_type_mapping
(
    id          BIGSERIAL PRIMARY KEY,
    db_type     VARCHAR(50)  NOT NULL,
    java_type   VARCHAR(100) NOT NULL,
    description VARCHAR(200)          DEFAULT '',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_type_mapping_db_type UNIQUE (db_type)
);
COMMENT
ON TABLE gen_type_mapping IS '数据库类型到Java类型映射表';
COMMENT
ON COLUMN gen_type_mapping.db_type IS '数据库字段类型（如 VARCHAR）';
COMMENT
ON COLUMN gen_type_mapping.java_type IS '映射的Java类型全名';
COMMENT
ON COLUMN gen_type_mapping.description IS '备注说明';
COMMENT
ON COLUMN gen_type_mapping.create_time IS '创建时间';