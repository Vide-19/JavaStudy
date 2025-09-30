package com.javastudy.my_project_backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
/**
 * JWT 工具类
 * 提供：
 * - 生成 Token
 * - 解析 Token
 * - 验证 Token
 * - 使 Token 失效（加入 Redis 黑名单）
 */
@Component
public class JwtUtils {
    // JWT 签名密钥
    @Value("${spring.security.jwt.key}")
    String key;
    // Token 有效期
    @Value("${spring.security.jwt.expire}")
    int expire;
    // Redis 模板，用于存储失效 Token
    @Resource
    StringRedisTemplate template;
    /**
     * 使 JWT 失效（加入黑名单）
     * @param headerToken Authorization 请求头中的 Token（格式：Bearer xxx）
     * @return 是否成功失效
     */
    public boolean invalidateJwt(String headerToken) {
        String token = this.convertToken(headerToken);  // 提取真实 Token
        if (token == null)
            return false;
        Algorithm algorithm = Algorithm.HMAC256(key);   //JWT 的签名是使用 SHA-256 哈希函数和指定密钥通过 HMAC 生成的
        JWTVerifier verifier = JWT.require(algorithm).build();  //验证 JWT 令牌
        //令牌失效，将令牌存至redis，给令牌加id，通过id查是否失效
        try {
            DecodedJWT jwt = verifier.verify(token);    // 验证 Token 并解析
            String id = jwt.getId();    // 获取 JWT ID（UUID）
            return deleteToken(id, jwt.getExpiresAt()); // 将 ID 加入黑名单
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    /**
     * 将 Token ID 加入 Redis 黑名单
     * @param uuid Token 的唯一 ID
     * @param time 过期时间
     * @return 是否成功
     */
    public boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid))  //已在黑名单中
            return false;
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);  //已过期，相减为负，取0
        //存入黑名单
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MICROSECONDS);
        return true;
    }
    /**
     * 判断 Token 是否已在黑名单中
     */
    public boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }
    /**
     * 解析并验证 JWT
     * @param headerToken Authorization 请求头
     * @return 解码后的 JWT，若无效则返回 null
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if(token == null)
            return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = verifier.verify(token);
            if (this.isInvalidToken(verify.getId()))    // 已失效
                return null;
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify; // 已过期则返回 null
        } catch (JWTVerificationException e) {
            return null;
        }
    }
    /**
     * 创建 JWT Token
     * @param details 用户详情
     * @param id 用户 ID
     * @param username 用户名
     * @return 签名后的 JWT 字符串
     */
    public String createJwt(UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expiresTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())    //随机唯一id
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }
    /**
     * 计算 Token 过期时间
     */
    public Date expiresTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }
    /**
     * 从 Authorization 头中提取 Token（去掉 "Bearer " 前缀）
     */
    public String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }
    /**
     * 从 JWT 中解析出 UserDetails 对象
     */
    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("123456")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }
    /**
     * 从 JWT 中解析出用户 ID
     */
    public Integer toId (DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }
}

