package com.seckill.exception;     // 异常类放在 exception 包

public class BusinessException extends RuntimeException {  // 继承 RuntimeException = 非受检异常（不需要 try-catch）
    private final int code;          // 错误码：400 为通用业务错误，可根据业务需求自定义

    public BusinessException(String message) {  // 构造函数1：只传错误消息，默认 code=400
        super(message);              // 调用父类 RuntimeException 的构造函数，保存错误消息
        this.code = 400;             // 默认错误码设为 400（请求参数有误）
    }

    public BusinessException(int code, String message) { // 构造函数2：自定义错误码和错误消息
        super(message);              // 把消息传给父类
        this.code = code;            // 保存自定义的错误码（比如 409 表示冲突）
    }

    public int getCode() { return code; }  // 获取错误码（方法名必须符合 JavaBean 规范：get + 首字母大写字段名）
}
