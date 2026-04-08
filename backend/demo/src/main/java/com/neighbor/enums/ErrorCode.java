package com.neighbor.enums;

public enum ErrorCode {
    SUCCESS(0, "成功"),
    UNAUTHORIZED(1001, "未登录或Token无效"),
    FORBIDDEN(1002, "无权限"),
    PARAM_ERROR(2001, "参数校验失败"),
    ITEM_NOT_FOUND(3001, "物品不存在"),
    ITEM_NOT_AVAILABLE(3002, "物品状态不可借"),
    CREDIT_NOT_ENOUGH(3003, "信用分不足"),
    ITEM_ALREADY_EXISTS(3004, "物品已存在"),
    CATEGORY_NOT_FOUND(3005, "分类不存在"),
    BORROW_NOT_FOUND(4001, "借阅记录不存在"),
    BORROW_STATUS_INVALID(4002, "借阅状态不允许当前操作"),
    USER_NOT_FOUND(4003, "用户不存在"),
    NOT_YOUR_BORROW_REQUEST(4004, "不是您的借阅请求"),
    CANNOT_BORROW_OWN_ITEM(4005, "不能借用自己发布的物品"),
    INVALID_DATE_RANGE(4006, "日期范围无效"),
    START_DATE_IN_PAST(4007, "开始日期不能早于今天"),
    REMIND_LIMIT_EXCEEDED(4008, "今日提醒次数已达上限"),
    NOT_YOUR_ITEM(4009, "不是您的物品"),
    REVIEW_NOT_FOUND(5001, "评价不存在"),
    REVIEW_ALREADY_EXISTS(5002, "该借阅已评价"),
    REVIEW_BORROW_NOT_RETURNED(5003, "借阅未完成，无法评价"),
    REVIEW_NOT_YOUR_BORROW(5004, "只能评价自己的借阅记录"),
    SYSTEM_ERROR(9000, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
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
