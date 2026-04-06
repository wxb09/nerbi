package com.neighbor.dto;

import java.util.Map;

public record PushNotification(
    String type,
    Long itemId,
    String itemStatus,
    Long pendingCount,
    String message
) {
    public static PushNotification newBorrowApply(Long itemId, Long pendingCount) {
        return new PushNotification("NEW_BORROW_APPLY", itemId, null, pendingCount, "您有新的借阅申请");
    }
    
    public static PushNotification requestApproved(Long itemId) {
        return new PushNotification("REQUEST_APPROVED", itemId, "BORROWED", null, "您的借阅申请已通过");
    }
    
    public static PushNotification requestRejected(Long itemId) {
        return new PushNotification("REQUEST_REJECTED", itemId, null, null, "您的借阅申请已拒绝");
    }
    
    public static PushNotification itemStatusChanged(Long itemId, String newStatus, Long pendingCount) {
        return new PushNotification("ITEM_STATUS_CHANGED", itemId, newStatus, pendingCount, "物品状态已变更");
    }
    
    public static PushNotification returnRequested(Long itemId, Long pendingCount) {
        return new PushNotification("RETURN_REQUESTED", itemId, null, pendingCount, "有物品申请归还");
    }
    
    public static PushNotification returnConfirmed(Long itemId, Long pendingCount) {
        return new PushNotification("RETURN_CONFIRMED", itemId, "AVAILABLE", pendingCount, "物品已归还");
    }
    
    public static PushNotification borrowReturned(Long itemId) {
        return new PushNotification("BORROW_RETURNED", itemId, null, null, "您借用的物品已确认归还");
    }
}
