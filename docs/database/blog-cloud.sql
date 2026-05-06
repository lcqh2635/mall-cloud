DROP DATABASE IF EXISTS `blog_cloud`;

CREATE DATABASE `blog_cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

USE `blog_cloud`;

-- ----------------------------
-- Table structure for t_admin
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin`  (
  `admin_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '管理员ID',
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '密码',
  `gender` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '性别(1-男 2-女)',
  `nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '个人头像',
  `email` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `birthday` date NULL DEFAULT NULL COMMENT '出生年月日',
  `mobile` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '手机',
  `description` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '自我简介',
  `login_count` int UNSIGNED NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '127.0.0.1' COMMENT '最后登录IP',
  `role_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '角色ID',
  `salt` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '随机盐',
  `status` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '1' COMMENT '状态(0-开启 1-关闭)',
  `del_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '0' COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`admin_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_blog
-- ----------------------------
DROP TABLE IF EXISTS `t_blog`;
CREATE TABLE `t_blog`  (
  `blog_id` bigint NOT NULL COMMENT '博客ID',
  `author` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '作者',
  `title` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '博客标题',
  `description` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '博客简介',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '博客内容',
  `click_count` int NULL DEFAULT 0 COMMENT '博客点击数',
  `tag_id` bigint NULL DEFAULT NULL COMMENT '标签ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '管理员ID',
  `picture_id` bigint NULL DEFAULT NULL COMMENT '标题图片ID',
  `blog_sort_id` bigint NULL DEFAULT NULL COMMENT '博客分类ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态(0-开启 1-关闭)',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`blog_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '博客表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_blog_sort
-- ----------------------------
DROP TABLE IF EXISTS `t_blog_sort`;
CREATE TABLE `t_blog_sort`  (
  `blog_sort_id` bigint NOT NULL COMMENT '分类ID',
  `sort_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '分类名称',
  `content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '分类简介',
  `sort` int NULL DEFAULT 0 COMMENT '排序字段，越大越靠前',
  `click_count` int NULL DEFAULT 0 COMMENT '点击数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-正常 1-不可用)',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`blog_sort_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '博客分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_category_menu
-- ----------------------------
DROP TABLE IF EXISTS `t_category_menu`;
CREATE TABLE `t_category_menu`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_level` tinyint(1) NULL DEFAULT NULL COMMENT '菜单级别',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `parent_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '父uid',
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'url地址',
  `icon` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序字段，越大越靠前',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `is_show` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否显示 1:是 0:否',
  `menu_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '菜单类型 0: 菜单   1: 按钮',
  `is_jump_external_url` tinyint(1) NULL DEFAULT 0 COMMENT '是否跳转外部链接 0：否，1：是',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_collect
-- ----------------------------
DROP TABLE IF EXISTS `t_collect`;
CREATE TABLE `t_collect`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `user_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户的uid',
  `blog_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '博客的uid',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `user_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '用户uid',
  `to_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '回复某条评论的uid',
  `to_user_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '回复某个人的uid',
  `content` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '评论内容',
  `blog_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '博客uid',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `source` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '评论来源： MESSAGE_BOARD，ABOUT，BLOG_INFO 等',
  `TYPE` tinyint(1) NOT NULL DEFAULT 0 COMMENT '评论类型 1:点赞 0:评论',
  `first_comment_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '一级评论UID',
  `del_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '0' COMMENT '0-正常，1-删除',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_exception_log
-- ----------------------------
DROP TABLE IF EXISTS `t_exception_log`;
CREATE TABLE `t_exception_log`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `exception_json` mediumtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '异常对象json格式',
  `exception_message` mediumtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '异常信息',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `ip` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'ip地址',
  `ip_source` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'ip来源',
  `method` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `operation` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '方法描述',
  `params` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '请求参数',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '异常日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_link
-- ----------------------------
DROP TABLE IF EXISTS `t_link`;
CREATE TABLE `t_link`  (
  `link_id` bigint NOT NULL COMMENT '唯一id',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '友情链接标题',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '友情链接介绍',
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '友情链接URL',
  `picture_id` bigint NULL DEFAULT NULL COMMENT '网站图标',
  `click_count` int NULL DEFAULT 0 COMMENT '点击数',
  `sort` int NULL DEFAULT 0 COMMENT '排序字段，越大越靠前',
  `user_id` bigint NULL DEFAULT NULL COMMENT '申请用户ID',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '操作管理员ID',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '站长邮箱',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '友链状态： 0 申请中， 1：已上线，  2：已下架',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`link_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '友情链接表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_menu
-- ----------------------------
DROP TABLE IF EXISTS `t_menu`;
CREATE TABLE `t_menu`  (
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '前端URL',
  `icon` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `type` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '菜单类型(0-菜单 1-按钮)',
  `permission` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '菜单权限标识',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父菜单ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态(0-开启 1-关闭)',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_picture
-- ----------------------------
DROP TABLE IF EXISTS `t_picture`;
CREATE TABLE `t_picture`  (
  `picture_id` bigint NOT NULL COMMENT '唯一ID',
  `picture_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '图片名',
  `picture_sort_id` bigint NULL DEFAULT NULL COMMENT '图片分类ID',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `del_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '0' COMMENT '0-正常，1-删除',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`picture_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_picture_sort
-- ----------------------------
DROP TABLE IF EXISTS `t_picture_sort`;
CREATE TABLE `t_picture_sort`  (
  `picture_sort_id` bigint NOT NULL COMMENT '唯一ID',
  `picture_sort_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '图片分类名',
  `parent_id` bigint NULL DEFAULT NULL,
  `sort` int NULL DEFAULT 0 COMMENT '排序字段，越大越靠前',
  `is_show` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示，0：是，1：否',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态(0-开启 1-关闭)',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`picture_sort_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '图片分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '角色名',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '角色介绍',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态(0-开启 1-关闭)',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `t_role_menu`;
CREATE TABLE `t_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '角色菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_data`;
CREATE TABLE `t_sys_dict_data`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `oid` int NOT NULL AUTO_INCREMENT COMMENT '自增键oid',
  `dict_type_uid` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '字典类型UID',
  `dict_label` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '字典标签',
  `dict_value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '字典键值',
  `css_class` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认（1是 0否）,默认为0',
  `create_by_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人UID',
  `update_by_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '最后更新人UID',
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `is_publish` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '1' COMMENT '是否发布(1:是，0:否)',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `oid`(`oid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_dict_type`;
CREATE TABLE `t_sys_dict_type`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `oid` int NOT NULL AUTO_INCREMENT COMMENT '自增键oid',
  `dict_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '字典名称',
  `dict_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '字典类型',
  `create_by_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '创建人UID',
  `update_by_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '最后更新人UID',
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `is_publish` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '1' COMMENT '是否发布(1:是，0:否)',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
  PRIMARY KEY (`uid`) USING BTREE,
  INDEX `oid`(`oid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_sys_log
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_log`;
CREATE TABLE `t_sys_log`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户名',
  `admin_uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '管理员uid',
  `ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求ip地址',
  `url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求url',
  `type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求方式',
  `class_path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求类路径',
  `method` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '请求方法名',
  `params` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '请求参数',
  `operation` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `ip_source` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'ip来源',
  `spend_time` int NULL DEFAULT 0 COMMENT '方法请求花费的时间',
  `del_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '0' COMMENT '0-正常，1-删除',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_sys_params
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_params`;
CREATE TABLE `t_sys_params`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `params_type` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '1' COMMENT '配置类型 是否系统内置(1:，是 0:否)',
  `params_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '参数名称',
  `params_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '参数键名',
  `remark` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `params_value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '参数键值',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序字段',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_system_config
-- ----------------------------
DROP TABLE IF EXISTS `t_system_config`;
CREATE TABLE `t_system_config`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `qi_niu_access_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '七牛云公钥',
  `qi_niu_secret_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '七牛云私钥',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱账号',
  `email_user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱发件人用户名',
  `email_password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱密码',
  `smtp_address` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'SMTP地址',
  `smtp_port` varchar(6) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '1' COMMENT 'SMTP端口',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `qi_niu_bucket` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '七牛云上传空间',
  `qi_niu_area` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '七牛云存储区域 华东（z0），华北(z1)，华南(z2)，北美(na0)，东南亚(as0)',
  `upload_qi_niu` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '1' COMMENT '图片是否上传七牛云 (0:否， 1：是)',
  `upload_local` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '1' COMMENT '图片是否上传本地存储 (0:否， 1：是)',
  `picture_priority` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '1' COMMENT '图片显示优先级（ 1 展示 七牛云,  0 本地）',
  `qi_niu_picture_base_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '七牛云域名前缀：http://images.moguit.cn',
  `local_picture_base_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '本地服务器域名前缀：http://localhost:8600',
  `start_email_notification` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '0' COMMENT '是否开启邮件通知(0:否， 1:是)',
  `editor_model` tinyint(1) NOT NULL DEFAULT 0 COMMENT '编辑器模式，(0：富文本编辑器CKEditor，1：markdown编辑器Veditor)',
  `theme_color` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL DEFAULT '#409EFF' COMMENT '主题颜色',
  `minio_end_point` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'Minio远程连接地址',
  `minio_access_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'Minio公钥',
  `minio_secret_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'Minio私钥',
  `minio_bucket` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'Minio桶',
  `upload_minio` tinyint(1) NOT NULL DEFAULT 0 COMMENT '图片是否上传Minio (0:否， 1：是)',
  `minio_picture_base_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'Minio服务器文件域名前缀',
  `open_dashboard_notification` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启仪表盘通知(0:否， 1:是)',
  `dashboard_notification` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL COMMENT '仪表盘通知【用于首次登录弹框】',
  `content_picture_priority` tinyint(1) NOT NULL DEFAULT 0 COMMENT '博客详情图片显示优先级（ 0:本地  1: 七牛云 2: Minio）',
  `open_email_activate` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启用户邮件激活功能【0 关闭，1 开启】',
  `search_model` tinyint(1) NOT NULL DEFAULT 0 COMMENT '搜索模式【0:SQL搜索 、1：全文检索】',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_tag
-- ----------------------------
DROP TABLE IF EXISTS `t_tag`;
CREATE TABLE `t_tag`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '标签内容',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `click_count` int NULL DEFAULT 0 COMMENT '标签简介',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `sort` int NULL DEFAULT 0 COMMENT '排序字段，越大越靠前',
  `del_flag` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '0' COMMENT '0-正常，1-删除',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '密码',
  `gender` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '性别(1-男 2-女)',
  `nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '个人头像',
  `email` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `birthday` date NULL DEFAULT NULL COMMENT '出生年月日',
  `mobile` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '手机',
  `description` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '自我简介',
  `login_count` int UNSIGNED NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '127.0.0.1' COMMENT '最后登录IP',
  `ip_source` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'ip来源',
  `browser` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `salt` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '随机盐',
  `status` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '状态(0-开启 1-关闭)',
  `del_flag` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '逻辑删除(0-正常 1-删除)',
  `create_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2024 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_user_role
-- ----------------------------
DROP TABLE IF EXISTS `t_user_role`;
CREATE TABLE `t_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '用户角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_visitor
-- ----------------------------
DROP TABLE IF EXISTS `t_visitor`;
CREATE TABLE `t_visitor`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '唯一uid',
  `user_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '邮箱',
  `login_count` int UNSIGNED NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '127.0.0.1' COMMENT '最后登录IP',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = '游客表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_web_config
-- ----------------------------
DROP TABLE IF EXISTS `t_web_config`;
CREATE TABLE `t_web_config`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `logo` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT 'logo(文件UID)',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '网站名称',
  `summary` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '介绍',
  `keyword` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '关键字',
  `author` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '作者',
  `record_num` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '备案号',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `ali_pay` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '支付宝收款码FileId',
  `weixin_pay` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '微信收款码FileId',
  `github` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'github地址',
  `gitee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'gitee地址',
  `qq_number` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'QQ号',
  `qq_group` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'QQ群',
  `we_chat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '微信号',
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `show_list` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '显示的列表（用于控制邮箱、QQ、QQ群、Github、Gitee、微信是否显示在前端）',
  `login_type_list` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '登录方式列表（用于控制前端登录方式，如账号密码,码云,Github,QQ,微信）',
  `open_comment` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT '1' COMMENT '是否开启评论(0:否 1:是)',
  `open_mobile_comment` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启移动端评论(0:否， 1:是)',
  `open_admiration` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启赞赏(0:否， 1:是)',
  `open_mobile_admiration` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启移动端赞赏(0:否， 1:是)',
  `link_apply_template` varchar(2018) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '友链申请模板,添加友链申请模板格式',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_web_navbar
-- ----------------------------
DROP TABLE IF EXISTS `t_web_navbar`;
CREATE TABLE `t_web_navbar`  (
  `uid` varchar(96) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `name` varchar(765) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `navbar_level` tinyint(1) NULL DEFAULT NULL,
  `summary` varchar(600) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `parent_uid` varchar(96) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `url` varchar(765) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `icon` varchar(150) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL,
  `is_show` tinyint(1) NULL DEFAULT NULL,
  `is_jump_external_url` tinyint(1) NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_web_visit
-- ----------------------------
DROP TABLE IF EXISTS `t_web_visit`;
CREATE TABLE `t_web_visit`  (
  `uid` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL COMMENT '主键',
  `user_uid` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '用户uid',
  `ip` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '访问ip地址',
  `behavior` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '用户行为',
  `module_uid` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '模块uid（文章uid，标签uid，分类uid）',
  `other_data` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '附加数据(比如搜索内容)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  `os` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `browser` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `ip_source` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NULL DEFAULT NULL COMMENT 'ip来源',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_unicode_ci COMMENT = 'Web访问记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
