package com.javastudy.my_project_backend.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.javastudy.my_project_backend.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * JWT 认证过滤器
 * 每次请求都会经过此过滤器：
 * - 检查是否有 Authorization 头
 * - 解析 JWT
 * - 若有效，则设置 SecurityContext，表示用户已登录
 */
@Component
public class JwtAutorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = utils.resolveJwt(authorization);
        if (jwt != null) {
            // Token 有效，构建 Authentication 对象
            UserDetails user = utils.toUser(jwt);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 设置到 SecurityContext，表示用户已认证
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // 将用户 ID 放入 request，供后续 Controller 使用
            request.setAttribute("id", utils.toId(jwt));
        }
        // 继续执行后续过滤器或 Controller
        filterChain.doFilter(request, response);
    }
}
