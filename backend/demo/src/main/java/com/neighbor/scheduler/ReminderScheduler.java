package com.neighbor.scheduler;

import com.neighbor.entity.Borrow;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.MessageType;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final BorrowRepository borrowRepository;
    private final MessageService messageService;

    public ReminderScheduler(BorrowRepository borrowRepository, MessageService messageService) {
        this.borrowRepository = borrowRepository;
        this.messageService = messageService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void checkAndRemind() {
        log.info("开始执行借阅提醒定时任务...");
        
        remindDueTomorrow();
        remindOverdue();
        
        log.info("借阅提醒定时任务执行完成");
    }

    private void remindDueTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Borrow> dueTomorrow = borrowRepository.findByStatusAndEndDateWithDetails(BorrowStatus.ACTIVE, tomorrow);
        
        log.info("找到 {} 条即将到期的借阅记录", dueTomorrow.size());
        
        for (Borrow borrow : dueTomorrow) {
            try {
                LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                boolean alreadyReminded = messageService.hasRecentMessage(
                    borrow.getBorrower().getId(),
                    MessageType.RETURN_DUE,
                    borrow.getId(),
                    todayStart
                );
                
                if (!alreadyReminded) {
                    messageService.sendBorrowNotification(borrow, MessageType.RETURN_DUE);
                    log.info("已发送到期提醒: 借阅ID={}, 借入者={}", borrow.getId(), borrow.getBorrower().getNickname());
                }
            } catch (Exception e) {
                log.error("发送到期提醒失败: 借阅ID={}", borrow.getId(), e);
            }
        }
    }

    private void remindOverdue() {
        LocalDate today = LocalDate.now();
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        
        List<Borrow> overdue = borrowRepository.findOverdueForReminder(
            Arrays.asList(BorrowStatus.ACTIVE, BorrowStatus.OVERDUE),
            today,
            twoDaysAgo
        );
        
        log.info("找到 {} 条超期借阅记录需要提醒", overdue.size());
        
        for (Borrow borrow : overdue) {
            try {
                if (borrow.getStatus() == BorrowStatus.ACTIVE) {
                    borrow.setStatus(BorrowStatus.OVERDUE);
                    borrowRepository.save(borrow);
                    log.info("已更新超期状态: 借阅ID={}", borrow.getId());
                }
                
                messageService.sendBorrowNotification(borrow, MessageType.RETURN_OVERDUE);
                borrow.setRemindCount(borrow.getRemindCount() + 1);
                borrow.setLastRemindAt(LocalDateTime.now());
                borrowRepository.save(borrow);
                
                log.info("已发送超期提醒: 借阅ID={}, 借入者={}", borrow.getId(), borrow.getBorrower().getNickname());
            } catch (Exception e) {
                log.error("发送超期提醒失败: 借阅ID={}", borrow.getId(), e);
            }
        }
    }
}
