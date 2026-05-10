package com.mallcloud.commons.cache.config;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 缓存 Key 生成器
 *
 * <p>负责根据注解配置生成最终的缓存 Key，支持三种策略：
 * <ol>
 *   <li>SpEL 表达式 — 最灵活，支持引用参数、返回值、方法信息</li>
 *   <li>自动生成    — 基于 {类名}:{方法名}:{参数列表MD5} 生成唯一 Key</li>
 *   <li>全量清除    — allEntries 场景下只需要 cacheName 前缀</li>
 * </ol>
 *
 * <p>最终 Key 格式：{l2KeyPrefix}{cacheName}:{generatedKey}
 * 示例："mallcloud:cache:user:12345"
 *
 * @author mallcloud
 */
public class CacheKeyGenerator {

    /**
     * SpEL 表达式解析器（线程安全，复用单例）
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 参数名解析器，用于将参数名注入 SpEL 上下文（支持 #参数名 语法）
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    /**
     * Key 各部分之间的分隔符
     */
    private static final String SEPARATOR = ":";

    /**
     * 生成完整缓存 Key
     *
     * @param keyPrefix L2 全局 Key 前缀（来自配置，如 "mallcloud:cache:"）
     * @param cacheName 缓存命名空间（来自注解，如 "user"）
     * @param spelKey   注解中配置的 SpEL Key 表达式，为空则自动生成
     * @param joinPoint AOP 切入点，用于获取方法信息和参数
     * @param result    方法返回值，SpEL 中可用 {@code #result} 引用（@CachePut 场景）
     * @return 完整缓存 Key 字符串
     */
    public String generate(String keyPrefix, String cacheName, String spelKey,
                           ProceedingJoinPoint joinPoint, Object result) {

        // 第一步：解析 SpEL 表达式或自动生成 Key 的"业务部分"
        String businessKey;
        if (StringUtils.hasText(spelKey)) {
            // 有 SpEL 表达式：解析后作为业务 Key
            businessKey = evaluateSpel(spelKey, joinPoint, result);
        } else {
            // 无 SpEL 表达式：自动生成，避免 Key 过长用 MD5 压缩参数部分
            businessKey = generateAutoKey(joinPoint);
        }

        // 第二步：拼装最终 Key：{prefix}{cacheName}:{businessKey}
        // 示例："mallcloud:cache:user:12345"
        return keyPrefix + cacheName + SEPARATOR + businessKey;
    }

    /**
     * 生成 cacheName 命名空间的通配符前缀（用于 allEntries 全量清除）
     *
     * @param keyPrefix L2 全局 Key 前缀
     * @param cacheName 缓存命名空间
     * @return 通配符 Key，如 "mallcloud:cache:user:*"
     */
    public String generatePattern(String keyPrefix, String cacheName) {
        return keyPrefix + cacheName + SEPARATOR + "*";
    }

    // ===================== 私有方法 =====================

    /**
     * 解析 SpEL 表达式，返回字符串形式的 Key
     *
     * <p>SpEL 上下文中注入的变量：
     * <ul>
     *   <li>所有方法参数（按参数名，如 #userId）</li>
     *   <li>{@code #p0, #p1, ...} — 按位置引用参数</li>
     *   <li>{@code #result} — 方法返回值（@CachePut 中使用）</li>
     *   <li>{@code #root.method} — 方法反射对象</li>
     *   <li>{@code #root.args} — 参数数组</li>
     * </ul>
     *
     * @param spelExpression SpEL 表达式字符串
     * @param joinPoint      AOP 切入点
     * @param result         方法返回值（可为 null）
     * @return 解析后的 Key 字符串
     */
    private String evaluateSpel(String spelExpression, ProceedingJoinPoint joinPoint, Object result) {
        try {
            Method method = getMethod(joinPoint);
            // 构建 SpEL 求值上下文，注入方法参数
            EvaluationContext context = new MethodBasedEvaluationContext(
                    joinPoint.getTarget(), method, joinPoint.getArgs(), NAME_DISCOVERER);
            // 注入返回值，支持 @CachePut 中用 #result 作为 Key
            context.setVariable("result", result);

            Expression expression = PARSER.parseExpression(spelExpression);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : "null";
        } catch (Exception e) {
            // SpEL 解析失败时降级为自动生成，避免缓存失效
            return generateAutoKey(joinPoint);
        }
    }

    /**
     * 自动生成 Key（无 SpEL 表达式时的兜底策略）
     *
     * <p>格式：{简单类名}#{方法名}#{参数MD5}
     * 示例：{@code UserService#getUserById#a665a45920422f9d417e4867efdc4fb8}
     *
     * <p>对参数列表取 MD5，避免参数过多或参数值过长导致 Redis Key 超长。
     *
     * @param joinPoint AOP 切入点
     * @return 自动生成的 Key
     */
    private String generateAutoKey(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 对参数做 MD5，参数为空时用 "no-args" 占位
        String argsKey = (args == null || args.length == 0)
                ? "no-args"
                : DigestUtils.md5DigestAsHex(Arrays.toString(args).getBytes(StandardCharsets.UTF_8));

        return className + "#" + methodName + "#" + argsKey;
    }

    /**
     * 从 AOP 切入点获取方法反射对象
     *
     * @param joinPoint AOP 切入点
     * @return 方法反射对象
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.getTarget().getClass().getMethod(
                    joinPoint.getSignature().getName(),
                    ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes()
            );
        } catch (NoSuchMethodException e) {
            return ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
        }
    }
}