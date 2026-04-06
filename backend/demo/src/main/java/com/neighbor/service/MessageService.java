package com.neighbor.service;

import com.neighbor.dto.MessageDTO;
import com.neighbor.entity.Borrow;
import com.neighbor.entity.Message;
import com.neighbor.entity.User;
import com.neighbor.enums.MessageType;
import com.neighbor.repository.MessageRepository;
import com.neighbor.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessages(Long userId, Pageable pageable) {
        return messageRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getUnreadMessages(Long userId, Pageable pageable) {
        return messageRepository.findByToUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return messageRepository.countByToUserIdAndIsRead(userId, false);
    }

    public void markAsRead(Long messageId, Long userId) {
        messageRepository.markAsRead(messageId, userId);
    }

    public int markAllAsRead(Long userId) {
        return messageRepository.markAllAsRead(userId);
    }

    public void sendMessage(Long toUserId, MessageType type, String title, String content, Long relatedId) {
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Message message = new Message();
        message.setToUser(toUser);
        message.setType(type.name());
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setIsRead(false);
        
        messageRepository.save(message);
    }

    public void sendBorrowNotification(Borrow borrow, MessageType type) {
        Long toUserId = null;
        String title = "";
        String content = "";
        
        switch (type) {
            case BORROW_APPLY -> {
                toUserId = borrow.getLender().getId();
                title = "新的借阅申请";
                content = String.format("%s 申请借用您的物品「%s」", 
                    borrow.getBorrower().getNickname(),
                    borrow.getItem().getName());
            }
            case BORROW_APPROVED -> {
                toUserId = borrow.getBorrower().getId();
                title = "借阅申请已通过";
                content = String.format("您对「%s」的借阅申请已通过，请及时取件", 
                    borrow.getItem().getName());
            }
            case BORROW_REJECTED -> {
                toUserId = borrow.getBorrower().getId();
                title = "借阅申请被拒绝";
                content = String.format("您对「%s」的借阅申请被拒绝", 
                    borrow.getItem().getName());
            }
            case RETURN_DUE -> {
                toUserId = borrow.getBorrower().getId();
                title = "物品即将到期";
                content = String.format("您借用的「%s」将于明天到期，请及时归还", 
                    borrow.getItem().getName());
            }
            case RETURN_OVERDUE -> {
                toUserId = borrow.getBorrower().getId();
                title = "物品已超期";
                content = String.format("您借用的「%s」已超期，请尽快归还", 
                    borrow.getItem().getName());
            }
            case RETURN_CONFIRM -> {
                toUserId = borrow.getLender().getId();
                title = "物品归还申请";
                content = String.format("%s 申请归还「%s」，请确认", 
                    borrow.getBorrower().getNickname(),
                    borrow.getItem().getName());
            }
            default -> {}
        }
        
        if (toUserId != null) {
            sendMessage(toUserId, type, title, content, borrow.getId());
        }
    }

    public boolean hasRecentMessage(Long toUserId, MessageType type, Long relatedId, LocalDateTime since) {
        return messageRepository.existsByToUserIdAndTypeAndRelatedIdAndCreatedAtAfter(toUserId, type, relatedId, since);
    }

    private MessageDTO toDTO(Message message) {
        MessageDTO.BorrowInfo borrowInfo = null;
        if (message.getRelatedId() != null) {
            borrowInfo = new MessageDTO.BorrowInfo(
                message.getRelatedId(),
                null,
                null,
                null
            );
        }
        
        MessageType type;
        try {
            type = MessageType.valueOf(message.getType());
        } catch (IllegalArgumentException e) {
            type = MessageType.SYSTEM;
        }
        
        return new MessageDTO(
            message.getId(),
            message.getType(),
            type.getDescription(),
            message.getTitle(),
            message.getContent(),
            message.getRelatedId(),
            message.getIsRead(),
            message.getCreatedAt(),
            borrowInfo
        );
    }
}
