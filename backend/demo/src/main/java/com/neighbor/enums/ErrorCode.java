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
    PAYMENT_NOT_FOUND(6001, "支付记录不存在"),
    PAYMENT_ALREADY_PAID(6002, "该借阅已支付"),
    PAYMENT_AMOUNT_ZERO(6003, "支付金额不能为零"),
    PAYMENT_ALIPAY_ERROR(6004, "支付宝接口错误"),
    PAYMENT_REFUND_FAILED(6005, "退款失败"),
    PAYMENT_NO_DEPOSIT(6006, "无押金可退"),
    PAYMENT_ALREADY_REFUNDED(6007, "押金已退还"),
    PAYMENT_STATUS_INVALID(6008, "支付状态不允许当前操作"),
    DEPOSIT_DISPUTE_NOT_FOUND(6101, "押金纠纷不存在"),
    DEPOSIT_DISPUTE_ALREADY_EXISTS(6102, "该借阅已有进行中的押金纠纷"),
    DEPOSIT_DISPUTE_ALREADY_HANDLED(6103, "押金纠纷已处理"),
    DEPOSIT_DISPUTE_NO_PERMISSION(6104, "无权操作该押金纠纷"),
    DEPOSIT_DISPUTE_CLAIM_EXCEED(6105, "申请扣款金额超过押金"),
    DEPOSIT_DISPUTE_DEDUCTION_EXCEED(6106, "扣款金额超过押金"),
    DEPOSIT_DISPUTE_INVALID_ACTION(6107, "无效的纠纷处理操作"),
    TRANSFER_FAILED(7001, "转账失败"),
    TRANSFER_ACCOUNT_NOT_BOUND(7002, "未绑定支付宝账号"),
    SENSITIVE_CONTENT(8001, "内容包含敏感词"),
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
