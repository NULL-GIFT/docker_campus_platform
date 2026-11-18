package com.campus_platform.exception;//package com.campus_platform.exception;
//
//import com.campus_platform.common.Result;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
///**
// * 全局异常处理器 (E同学的任务)
// * 作用：拦截所有报错，统一返回 JSON 格式
// */
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    /**
//     * 拦截业务异常 (RuntimeException)
//     * 比如：我们在 Service 里写的 throw new RuntimeException("余额不足");
//     */
//    @ExceptionHandler(RuntimeException.class)
//    public Result handleRuntimeException(RuntimeException e) {
//        // 直接把错误信息返给前端，状态码设为 error
//        return Result.error(e.getMessage());
//    }
//
//    /**
//     * 拦截系统异常 (兜底)
//     * 比如：空指针、数据库连不上等意料之外的错误
//     */
//    @ExceptionHandler(Exception.class)
//    public Result handleException(Exception e) {
//        e.printStackTrace(); // 在控制台打印错误堆栈，方便排错
//        return Result.error("系统繁忙，请稍后再试: " + e.getMessage());
//    }
//}