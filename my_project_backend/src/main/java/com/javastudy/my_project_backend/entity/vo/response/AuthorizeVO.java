package com.javastudy.my_project_backend.entity.vo.response;

import lombok.Data;

import java.util.Date;
/**
 * 登录成功后返回的响应数据对象
 * 包含：
 * - 用户名
 * - 角色（暂未使用）
 * - JWT Token
 * - 过期时间
 */
@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
