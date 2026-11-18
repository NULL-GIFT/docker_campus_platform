package com.campus_platform.common;


/**
 * 大家写 Controller 的时候，返回类型统一写 Result！
 *
 * 如果你查到了数据，就 return Result.success(data);
 *
 * 如果你报错了，就 return Result.error("你的报错信息");
 * 千万别直接返回 null 或者 true/false！
 *
 */

/**
 * 统一响应结果类
 * 所有的接口返回类型必须是这个！
 */
public class Result<T> {
    private String code; // 状态码：200成功，500失败
    private String msg;  // 提示信息："操作成功" 或 "余额不足"
    private T data;      // 数据：具体的 User对象、List<Task> 等

    // 空构造
    public Result() {
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ============ 快捷方法 (给队友用的) ============

    // 1. 成功，不带数据 (比如：删除成功)
    public static <T> Result<T> success() {
        return new Result<>("200", "操作成功", null);
    }

    // 2. 成功，带数据 (比如：查询成功，返回列表)
    public static <T> Result<T> success(T data) {
        return new Result<>("200", "操作成功", data);
    }

    // 3. 失败 (比如：密码错误)
    public static <T> Result<T> error(String msg) {
        return new Result<>("500", msg, null);
    }

    // 4. 自定义失败 (比如：403 无权限)
    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    // ============ Getter & Setter (必须有) ============
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
