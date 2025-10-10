package com.javastudy.my_project_backend.config;

import com.javastudy.my_project_backend.entity.RestBean;
import com.javastudy.my_project_backend.entity.dto.Account;
import com.javastudy.my_project_backend.entity.vo.response.AuthorizeVO;
import com.javastudy.my_project_backend.filter.JwtAutorizeFilter;
import com.javastudy.my_project_backend.service.AccountService;
import com.javastudy.my_project_backend.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

//SpringSecurity配置类
@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils; // JWT 工具类，用于生成、解析、验证 Token
    @Resource
    JwtAutorizeFilter jwtAutorizeFilter;    // 自定义 JWT 过滤器，用于每次请求时检查 Token
    @Resource
    AccountService service;
    /**
     * 安全过滤链配置
     * 定义了整个项目的安全规则
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 配置 URL 权限
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**", "/error").permitAll()    // 所有 /api/auth 开头的接口（如登录）无需认证
                        .anyRequest().authenticated()   // 其他所有请求都必须认证
                )
                // 表单登录配置
                .formLogin(conf -> conf
                        .loginPage("/api/auth/login")   // 登录请求地址
                        .failureHandler(this::onAuthenticationFailure)  // 登录失败处理器
                        .successHandler(this::onAuthenticationSuccess)  // 登录成功处理器
                )
                // 登出配置
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")  // 登出请求地址
                        .logoutSuccessHandler(this::onLogoutSuccess)    // 登出成功处理器
                )
                // 异常处理配置
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized) // 未登录访问受保护资源时的处理
                        .accessDeniedHandler(this::onAccessDeny)    // 登录了但权限不足时的处理
                )
                // 禁用 CSRF（因为是前后端分离，使用 JWT，不需要 CSRF 防护）
                .csrf(AbstractHttpConfigurer::disable)
                // 会话管理：使用无状态（STATELESS），不创建 HttpSession
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 在 UsernamePasswordAuthenticationFilter 之前添加自定义 JWT 过滤器
                // 用于在每次请求中检查 JWT 是否有效，并设置 SecurityContext
                .addFilterBefore(jwtAutorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    /**
     * 登录失败处理器
     * 返回 401 Unauthorized 的 JSON 响应
     */
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }
    /**
     * 登录成功处理器
     * 生成 JWT Token 并返回包含 token、用户名、过期时间等信息的 JSON 响应
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        User user = (User) authentication.getPrincipal();   // 获取登录成功的用户信息
        Account account = service.findAccountByNameOrEmail(user.getUsername());
        String token = utils.createJwt(user, account.getId(), account.getUsername());  // 生成 JWT Token
        AuthorizeVO vo = new AuthorizeVO();
        BeanUtils.copyProperties(account, vo);  //快速将dto对象内容， 转至vo对象
        vo.setExpire(utils.expiresTime());
        //vo.setRole(account.getRole());
        vo.setToken(token);
        //vo.setUsername(account.getUsername());
        response.getWriter().write(RestBean.success(vo).asJsonString());
    }
    /**
     * 登出成功处理器
     * 将当前 Token 加入黑名单（Redis），使其失效
     */
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");  // 获取请求头中的 Token
        if (utils.invalidateJwt(authorization)) // 将 Token 加入黑名单
            writer.write(RestBean.success().asJsonString());    //成功
        else
            writer.write(RestBean.failure(400, "退出登录失败").asJsonString());   //失败
    }
    /**
     * 未认证异常处理（用户未登录）
     * 返回 401 Unauthorized
     */
    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }
    /**
     * 权限不足处理（用户已登录但无权限）
     * 返回 403 Forbidden
     */
    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.forbidden(accessDeniedException.getMessage()).asJsonString());
    }
}
