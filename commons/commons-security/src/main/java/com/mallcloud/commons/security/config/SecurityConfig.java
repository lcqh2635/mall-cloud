package com.mallcloud.commons.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;
import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailService userDetailService;
    private final SmsService tencentSmsService;

    // 访问白名单，在该类初始化的时候就需要获取
    //    String[] whitelist = {"/sms/**","/auth/**","/actuator/**"};
    //    private static String[] whitelist = {"/sms/**","/auth/**","/actuator/**"};
    private static String[] whitelist = {};

    // 可以在静态代码块中完成，当类被加载时被初始化
    static {
        // TODO 查询数据库白名单表
        List<String> result = new ArrayList<>();
        result.add("/sms/**");
        result.add("/auth/**");
        result.add("/actuator/**");
        if (!CollectionUtils.isEmpty(result)) {
            whitelist = result.toArray(String[]::new);
        }
    }

    // Spring Security 提供全面的 OAuth 2.0 支持。如何将 OAuth 2.0 集成到基于 servlet 的应用程序中。 https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html#oauth2-client
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(whitelist).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf((csrf) -> csrf.ignoringRequestMatchers("/token"))
                .rememberMe((rememberMe) -> rememberMe.rememberMeParameter("remember-me").key(""))
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults())
                        .opaqueToken(Customizer.withDefaults())
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );

        return http.build();
    }

    /*
     * Spring Boot 为 OAuth 2.0 登录提供了完整的自动配置功能。
     * 本节将展示如何使用 Google 作为身份验证提供程序来配置 OAuth 2.0 登录示例，并涵盖以下主题：
     * 1、初始设置： 要使用 Google 的 OAuth 2.0 身份验证系统登录，必须在 Google API 控制台中设置一个项目，以获取 OAuth 2.0 凭据。
     * 2、设置重定向 URI： 在 Google API 控制台中，必须设置一个重定向 URI，以便 Google 可以将用户重定向到我们的应用程序。
     * 重定向 URI 是最终用户的用户代理在同意页面上通过 Google 身份验证并授权访问 OAuth 客户端（在上一步中创建）后，重定向回应用程序中的路径。
     * 在 “设置重定向 URI ”分节中，确保授权重定向 URI 字段设置为 localhost:8080/login/oauth2/code/google。
     * 3、配置应用程序.yaml 文件： 在应用程序.yaml 文件中，必须设置 Google OAuth 2.0 身份验证的相关属性。
     * 4、启动应用程序： 启动 Spring Boot 示例并访问 localhost:8080。然后，您会被重定向到默认的自动生成登录页面，该页面会显示一个 Google 链接。
     * */


    // WebFlux 应用环境配置 SecurityWebFilterChain 参考 https://docs.spring.io/spring-security/reference/reactive/getting-started.html
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .x509(withDefaults())
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/resources/**", "/signup", "/about").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .pathMatchers("/db/**").access((authentication, context) ->
                                hasRole("ADMIN").check(authentication, context)
                                        .filter(decision -> !decision.isGranted())
                                        .switchIfEmpty(hasRole("DBA").check(authentication, context))
                        )
                        .anyExchange().denyAll()
                );
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = new ArrayList<>(3);
        // 可以设置多个认证处理器
        AuthenticationProvider usernameAuthenticationProvider = new DaoAuthenticationProvider();
        MobileAuthenticationProvider mobileAuthenticationProvider = new MobileAuthenticationProvider(userDetailService,tencentSmsService);
        providers.add(usernameAuthenticationProvider);
        providers.add(mobileAuthenticationProvider);
        return new ProviderManager(providers);
    }

}

// 笔记1： 凡是在springsecurity的认证和授权过程中抛出的异常，不论是我们认为抛出的异常还是走源码过程抛出的异常都会被springsecurity过滤器链中的ExceptionTranslationFilter异常处理的过滤器捕获
// 并调用相应的接口方法进行异常处理，所以会导致这些异常的响应结果和我们自定义的响应结果不一致的情况，为了保证给前端的响应结果的一致性，我们有必要对相应的接口
// 进行自定义的实现，以此来保证响应结果的统一性。同时在完成自定义处理之后，我们必须将实现注册成Bean，并将他们加入到springsecurity过滤环节的对应节点，
// 也就是在HttpSecurity对象的对应节点进行配置，以此确保自定义实现的组件能够生效。相应接口有：认证失败处理AuthenticationEntryPoint、鉴权失败处理AccessDeniedHandler

// 笔记2：  自从springsecurity-5.7.X版本开始，官方将废弃WebSecurityConfigurerAdapter这个springsecurity的适配器类，转而推荐使用向容器注入SecurityFilterChain
// 安全过滤器链的方法配置springsecurity

// 笔记3：  spring官方推荐使用构造函数的方式完成依赖注入，相应的我们可以使用lombok的相应注解完成，在开发过程中应当减少使用像@Autowired、@Resource的注解

// 笔记4： 在我们对springsecurity进行配置时，如果我们不对HttpSecurity对象配置formLogin属性的话，则在过滤器链中将不会存在UsernamePasswordAuthenticationFilter过滤器
// 原因是在springsecurity的默认配置中为HttpSecurity对象配置了formLogin属性，在该属性中配置了一个默认的登陆页面，同时new了一个UsernamePasswordAuthenticationFilter放了容器
// 但如果我们自定义配置springsecurity时没有配置formLogin属性则不会走UsernamePasswordAuthenticationFilter这一套的过滤器逻辑。所以，对于认证和授权的实现方案，我们可以总结为两套，
// 一套是，走我们自定义的过滤器并没有配置formLogin属性的认证方案；另一套是，走springsecurity默认的通过走UsernamePasswordAuthenticationFilter的这一套方案，走这套方案需要我们
// 在对springsecurity进行自定配置是为HttpSecurity对象配置formLogin属性，此外，我们还可以对这套方案中的一些流程处理进行自定义实现，比如自定义实现AuthenticationSuccessHandler
// AuthenticationFailureHandler等一些关键性流程的自我定制化实现，以此来更好的完成我们的认证授权。此外、如果我们也想对登出操作进行自定义的话，我们同理可以对HttpSecurity对象配置logout属性
// 然后自定义实现LogoutSuccessHandler

// SecurityContextHolderFilter、AuthorizationFilter
// 如果我们不是用默认的formLogin的方式，则需要自定义认证过滤器，然后在其中使用 AuthenticationManager 来认证登录信息
