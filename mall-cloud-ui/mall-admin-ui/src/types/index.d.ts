// 全局公共业务类型声明
/**
 * 基础对象类型 → interface
 * 工具类型/复杂类型 → type
 * 保持项目风格统一 比选择哪个更重要
 */

/**
 * 通用响应结构
 * @template T - 数据类型
 */
export interface BaseResponse<T = any> {
    /**
     * 状态码
     */
    code: number;
    /**
     * 数据
     */
    data: T;
    /**
     * 消息
     */
    message: string;
}

/**
 * 分页查询参数基础接口
 */
export interface PageQuery {
    pageNum: number;
    pageSize: number;
}

/**
 * 分页响应结果基础接口
 * @template T - 数据类型
 */
export interface PageResult<T> {
    /**
     * 数据列表
     */
    list: T[];
    /**
     * 总条数
     */
    total: number;
    /**
     * 当前页码
     */
    pageNum: number;
    /**
     * 每页条数
     */
    pageSize: number;
}


// 5. 实际业务相关的综合类型示例
/**
 * 用户信息
 */
export interface UserInfo {
    id: number;
    username: string;
    nickname: string;
    avatar: string;
    roles: string[];
    permissions: string[];
    email?: string;
    phone?: string;
    createTime: string;
}

/**
 * 系统配置项
 */
export interface SystemConfig {
    theme: 'light' | 'dark';
    primaryColor: string;
    layout: 'vertical' | 'horizontal' | 'mix';
    collapsed: boolean;
    showBreadcrumb: boolean;
    showTabs: boolean;
    showFooter: boolean;
}

interface User {
    id: number;
    name: string;
    age?: number;
}

// 可通过 extends 继承；可用 implements 实现
interface Admin extends User {
    role: string;
}


/**
 * 通用键值对类型
 */
type KeyValue<T = string> = Record<string, T>;

/**
 * 树形节点接口
 * @template T - 数据类型
 */
export interface TreeNode<T = KeyValue> {
    id: string | number;
    parentId: string | number | null;
    children?: TreeNode<T>[];
    [key: string]: any;
}

/**
 * 菜单项类型
 */
export type MenuItem = {
    id: number;
    parentId: number | null;
    name: string;
    path: string;
    component?: string | (() => Promise<typeof import('*.vue')>);
    redirect?: string;
    meta: {
        title: string;
        icon?: string;
        hidden?: boolean;
        keepAlive?: boolean;
        alwaysShow?: boolean;
        breadcrumb?: boolean;
        affix?: boolean;
        activeMenu?: string;
        roles?: string[];
    };
    children?: MenuItem[];
};