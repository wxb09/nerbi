package com.neighbor.chat.service;

import com.neighbor.chat.dto.ChatMessageDTO;
import com.neighbor.chat.dto.ConversationDTO;
import com.neighbor.chat.dto.SendMessageRequest;
import com.neighbor.chat.dto.UserStatusDTO;
import com.neighbor.chat.entity.ChatConversation;
import com.neighbor.chat.entity.ChatMessage;
import com.neighbor.chat.repository.ChatConversationRepository;
import com.neighbor.chat.repository.ChatMessageRepository;
import com.neighbor.entity.User;
import com.neighbor.repository.UserRepository;
import com.neighbor.websocket.WebSocketMessageHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WebSocketMessageHandler webSocketMessageHandler;

    public ChatService(ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       UserRepository userRepository,
                       WebSocketMessageHandler webSocketMessageHandler) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.webSocketMessageHandler = webSocketMessageHandler;
    }

    public ConversationDTO getOrCreateConversation(Long currentUserId, Long otherUserId) {
        Long user1Id = Math.min(currentUserId, otherUserId);
        Long user2Id = Math.max(currentUserId, otherUserId);
        
        ChatConversation conversation = conversationRepository
            .findByUser1IdAndUser2Id(user1Id, user2Id)
            .orElseGet(() -> {
                ChatConversation newConv = new ChatConversation();
                newConv.setUser1Id(user1Id);
                newConv.setUser2Id(user2Id);
                return conversationRepository.save(newConv);
            });
        
        return toConversationDTO(conversation, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(Long userId) {
        List<ChatConversation> conversations = conversationRepository
            .findByUserIdOrderByLastMessageAtDesc(userId);
        
        return conversations.stream()
            .map(conv -> toConversationDTO(conv, userId))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getMessages(Long conversationId, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        
        return messages.map(this::toMessageDTO);
    }

    public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("发送者不存在"));
        
        User receiver = userRepository.findById(request.receiverId())
            .orElseThrow(() -> new RuntimeException("接收者不存在"));
        
        ChatConversation conversation = getOrCreateConversationEntity(senderId, request.receiverId());
        
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(request.receiverId());
        message.setContent(request.content());
        message.setType(request.type() != null ? request.type() : "TEXT");
        message.setIsRead(false);
        
        ChatMessage saved = messageRepository.save(message);
        
        conversation.setLastMessageId(saved.getId());
        conversation.setLastMessageAt(saved.getCreatedAt());
        conversationRepository.save(conversation);
        
        ChatMessageDTO messageDTO = toMessageDTO(saved);
        
        pushChatMessage(receiver.getId(), messageDTO);
        
        return messageDTO;
    }

    public void markAsRead(Long conversationId, Long userId) {
        messageRepository.markAsReadByConversationAndReceiver(conversationId, userId);
    }

    @Transactional(readOnly = true)
    public UserStatusDTO getUserStatus(Long userId) {
        boolean isOnline = webSocketMessageHandler.isUserOnline(userId);
        return new UserStatusDTO(userId, isOnline);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadByReceiver(userId);
    }

    private ChatConversation getOrCreateConversationEntity(Long userId1, Long userId2) {
        Long user1Id = Math.min(userId1, userId2);
        Long user2Id = Math.max(userId1, userId2);
        
        return conversationRepository
            .findByUser1IdAndUser2Id(user1Id, user2Id)
            .orElseGet(() -> {
                ChatConversation newConv = new ChatConversation();
                newConv.setUser1Id(user1Id);
                newConv.setUser2Id(user2Id);
                return conversationRepository.save(newConv);
            });
    }

    private void pushChatMessage(Long receiverId, ChatMessageDTO message) {
        Map<String, Object> payload = Map.of(
            "type", "CHAT_MESSAGE",
            "data", message
        );
        webSocketMessageHandler.sendMessageToUser(receiverId, payload);
    }

    private ConversationDTO toConversationDTO(ChatConversation conversation, Long currentUserId) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) 
            ? conversation.getUser2Id() 
            : conversation.getUser1Id();
        
        User otherUser = userRepository.findById(otherUserId).orElse(null);
        
        ConversationDTO.UserInfo userInfo = null;
        if (otherUser != null) {
            boolean isOnline = webSocketMessageHandler.isUserOnline(otherUserId);
            userInfo = new ConversationDTO.UserInfo(
                otherUser.getId(),
                otherUser.getNickname(),
                otherUser.getAvatar(),
                isOnline
            );
        }
        
        ConversationDTO.LastMessage lastMessage = null;
        if (conversation.getLastMessageId() != null) {
            ChatMessage msg = messageRepository.findById(conversation.getLastMessageId()).orElse(null);
            if (msg != null) {
                lastMessage = new ConversationDTO.LastMessage(
                    msg.getContent(),
                    msg.getType(),
                    msg.getCreatedAt(),
                    msg.getIsRead()
                );
            }
        }
        
        Long unreadCount = messageRepository.countUnreadByConversationAndReceiver(
            conversation.getId(), currentUserId
        );
        
        return new ConversationDTO(
            conversation.getId(),
            userInfo,
            lastMessage,
            unreadCount
        );
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        return new ChatMessageDTO(
            message.getId(),
            message.getConversationId(),
            message.getSenderId(),
            message.getReceiverId(),
            message.getContent(),
            message.getType(),
            message.getIsRead(),
            message.getCreatedAt()
        );
    }
}
