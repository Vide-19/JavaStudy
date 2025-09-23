package com.javastudy.my_project_backend.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record RestBean<T>(int code, T data, String message) {
    //登录成功
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, data, "请求成功!");
    }
    public static <T> RestBean<T> success() {
        return success(null);
    }
    //登录失败
    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(code, null, message);
    }

    public String asJsonString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}
