package com.seckill.exception;

import com.seckill.vo.Result;                        // 统一响应格式
import lombok.extern.slf4j.Slf4j;                     // Lombok：自动生成 log 对象，用于打印日志
import org.springframework.dao.DuplicateKeyException;  // Spring 对数据库唯一约束冲突的封装异常
import org.springframework.http.HttpStatus;            // HTTP 状态码枚举
import org.springframework.web.bind.annotation.ExceptionHandler;      // 标记方法是异常处理器
import org.springframework.web.bind.annotation.ResponseStatus;         // 设置返回的 HTTP 状态码
import org.springframework.web.bind.annotation.RestControllerAdvice;   // = @ControllerAdvice + @ResponseBody，全局异常拦截

@Slf4j                         // Lombok 注解：自动生成 private static final Logger log = LoggerFactory.getLogger(...)
@RestControllerAdvice          // 全局异常处理器的核心注解：拦截所有 Controller 抛出的异常
public class GlobalExceptionHandler {  // "安全网"：不管哪里出了错，这里统一处理，返回友好 JSON

    @ExceptionHandler(BusinessException.class)  // 只捕获 BusinessException 及其子类
    public Result<?> handleBusinessException(BusinessException e) {  // e 就是被抛出的异常对象
        log.warn("Business exception: {}", e.getMessage());  // 记录警告日志，{} 是占位符，会被 e.getMessage() 替换
        return Result.fail(e.getCode(), e.getMessage());     // 返回 JSON 给前端，如 {"code":400,"message":"用户名已存在"}
    }

    @ExceptionHandler(DuplicateKeyException.class)  // 捕获数据库唯一索引冲突（比如重复注册、重复下单）
    @ResponseStatus(HttpStatus.CONFLICT)            // HTTP 状态码设为 409 Conflict
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("Duplicate key: {}", e.getMessage());
        return Result.fail(409, "重复操作");         // 不暴露具体是哪一列冲突，返回通用提示
    }

    @ExceptionHandler(Exception.class)              // 兜底处理器：捕获上面没覆盖到的所有其他异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
    public Result<?> handleException(Exception e) {
        log.error("Unexpected error", e);           // 记录完整堆栈信息到日志，方便排查
        return Result.fail(500, "服务器内部错误");    // 前端只看到通用提示，不暴露内部细节
    }
}
