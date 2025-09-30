package com.javastudy.my_project_backend.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
/**
 * 通用响应封装类（使用 Java 14+ Record）
 * 用于统一返回 JSON 格式：{ code: 200, data: {}, message: "ok" }
 *
 * @param <T> 响应数据类型
 */
public record RestBean<T>(int code, T data, String message) {
    /**
     * 成功响应（带数据）
     */
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, data, "请求成功!");
    }
    /**
     * 成功响应（无数据）
     */
    public static <T> RestBean<T> success() {
        return success(null);
    }
    /**
     * 未认证（401）
     */
    public static <T> RestBean<T> unauthorized(String message) {
        return failure(401, message);
    }
    /**
     * 权限不足（403）
     */
    public static <T> RestBean<T> forbidden(String message) {
        return failure(403, message);
    }
    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(code, null, message);
    }
    /**
     * 将对象序列化为 JSON 字符串
     * 支持 null 值输出（WriteNulls）
     */
    public String asJsonString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
