package com.gb.test.springsecuritydemo.enums;

public enum ResponseCodeEnum {
    // ... 其他业务状态码 ...

    // --- 安全相关的状态码 ---
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "权限不足");

    private final int code;
    private final String message;

    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}