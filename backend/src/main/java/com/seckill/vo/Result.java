package com.seckill.vo;            // VO = View Object（视图对象），给前端看的数据结构

import lombok.AllArgsConstructor;   // 生成包含所有字段的构造函数
import lombok.Data;                 // 生成 getter/setter
import lombok.NoArgsConstructor;    // 生成无参构造函数（JSON 反序列化需要）

@Data                                // 自动生成 getter/setter/toString/equals/hashCode
@AllArgsConstructor                  // 生成 new Result<>(200, "success", data) 这种全参构造函数
@NoArgsConstructor                   // 生成 new Result<>() 这种无参构造函数（Spring/Jackson 反序列化需要）
public class Result<T> {             // <T> 是泛型：data 字段可以是任意类型（String、User、List 等）
    private int code;                // 状态码：200=成功, 400=业务错误, 401=未登录, 409=冲突, 500=服务器错误
    private String message;          // 提示信息：成功时是 "success"，失败时是具体的错误原因
    private T data;                  // 泛型数据：注册时 data 为空，登录时 data 是 JWT 字符串，查询时 data 是列表

    // ========== 静态工厂方法（方便使用，不用每次 new Result<>(...)）==========

    public static <T> Result<T> ok(T data) {          // 成功 + 有数据：Result.ok(user) → {"code":200,"data":{...}}
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> ok() {                // 成功 + 无数据：Result.ok() → {"code":200,"data":null}
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> fail(String message) { // 失败（通用）：Result.fail("用户名已存在") → {"code":500,"message":"用户名已存在"}
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> fail(int code, String message) { // 失败（自定义状态码）：Result.fail(401, "未登录")
        return new Result<>(code, message, null);
    }
}
