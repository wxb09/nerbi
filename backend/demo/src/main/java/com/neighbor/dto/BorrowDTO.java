package com.neighbor.dto;

import com.neighbor.enums.BorrowStatus;
import java.time.LocalDate;

public record BorrowDTO(
    Long id,
    ItemInfo item,
    UserInfo borrower,
    UserInfo lender,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate actualReturnDate,
    BorrowStatus status,
    String purpose,
    String rejectReason
) {
    public record ItemInfo(
        Long id,
        String name,
        String image
    ) {}
    
    public record UserInfo(
        Long id,
        String nickname,
        String avatar
    ) {}
}
