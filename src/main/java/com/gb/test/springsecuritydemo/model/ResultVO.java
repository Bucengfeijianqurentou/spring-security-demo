package com.gb.test.springsecuritydemo.model;

import com.gb.test.springsecuritydemo.enums.ResponseCodeEnum;
import java.io.Serializable;

// 泛型 T 表示 data 字段的类型
public class ResultVO<T> implements Serializable {

    private boolean success;
    private int code;
    private String message;
    private T data; // 泛型 data

    // --- 构造函数 ---
    public ResultVO(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- 静态工厂方法 ---

    // 成功的
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(true, 200, "Success", data);
    }

    public static <T> ResultVO<T> success() {
        return success(null);
    }

    // 失败的 (使用枚举)
    public static <T> ResultVO<T> fail(ResponseCodeEnum codeEnum) {
        return new ResultVO<>(false, codeEnum.getCode(), codeEnum.getMessage(), null);
    }

    // 失败的 (自定义消息)
    public static <T> ResultVO<T> fail(int code, String message) {
        return new ResultVO<>(false, code, message, null);
    }

    // ... 必须为所有字段提供 Getter 和 Setter ...

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}