package com.elibrary.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 成功
    SUCCESS(200, "成功"),

    // 系统错误 (1xxx)
    SYSTEM_ERROR(1000, "系统错误"),
    DATABASE_ERROR(1001, "数据库错误"),
    NETWORK_ERROR(1002, "网络错误"),
    FILE_OPERATION_ERROR(1003, "文件操作错误"),

    // 参数错误 (2xxx)
    PARAMETER_ERROR(2000, "参数错误"),
    PARAMETER_EMPTY(2001, "参数为空"),
    PARAMETER_INVALID(2002, "参数无效"),

    // 业务错误 (3xxx)
    BOOK_NOT_FOUND(3000, "书籍不存在"),
    BOOK_UNAVAILABLE(3001, "书籍不可用"),
    USER_NOT_FOUND(3002, "用户不存在"),
    BORROW_RECORD_NOT_FOUND(3003, "借阅记录不存在"),
    BORROW_LIMIT_EXCEEDED(3004, "借阅数量超出限制"),
    BOOK_ALREADY_BORROWED(3005, "书籍已被借阅"),
    BORROW_OVERDUE(3006, "借阅逾期"),
    CANNOT_RENEW_BORROW(3007, "无法续借"),
    INSUFFICIENT_PERMISSION(3008, "权限不足"),

    // 认证授权错误 (4xxx)
    UNAUTHORIZED(4001, "未授权"),
    TOKEN_EXPIRED(4002, "Token已过期"),
    TOKEN_INVALID(4003, "Token无效"),
    ACCESS_DENIED(4004, "访问被拒绝"),

    // 数据错误 (5xxx)
    DATA_DUPLICATE(5000, "数据重复"),
    DATA_CONSISTENCY_ERROR(5001, "数据一致性错误"),
    DATA_VALIDATION_ERROR(5002, "数据验证失败");

    private final int code;
    private final String message;

    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}