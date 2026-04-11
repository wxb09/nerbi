package com.neighbor.auth;

import com.neighbor.auth.dto.LoginPhoneRequest;
import com.neighbor.auth.dto.LoginResponse;
import com.neighbor.auth.dto.UserInfo;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.User;
import com.neighbor.enums.UserStatus;
import com.neighbor.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse loginByPhone(LoginPhoneRequest req) {
        if (!"123456".equals(req.verifyCode())) {
            throw new BusinessException(2001, "验证码错误");
        }
        
        Optional<User> userOptional = userRepository.findByPhone(req.phone());
        User user;
        
        if (userOptional.isEmpty()) {
            user = new User();
            user.setPhone(req.phone());
            user.setNickname("新用户");
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(com.neighbor.enums.UserRole.USER);
            user.setCreditScore(new java.math.BigDecimal(10.00));
            user.setBorrowCount(0);
            user.setLendCount(0);
            user.setCo2Saved(0);
            user = userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(1002, "账号已被封禁，请联系管理员");
        }
        
        String role = user.getRole() != null ? user.getRole().name() : "USER";

        UserInfo userInfo = new UserInfo(
                user.getId().toString(),
                user.getNickname(),
                user.getAvatar(),
                user.getCommunity() != null ? user.getCommunity().getId().toString() : "",
                role
        );
        
        String token = jwtService.generateToken(user.getId().toString(), req.phone(), role);
        return new LoginResponse(token, userInfo);
    }

    public Map<String, Object> sendVerifyCode(String phone) {
        return Map.of("success", true, "expireSeconds", 60, "mockCode", "123456", "phone", phone);
    }

    public Map<String, Object> guestToken() {
        String token = jwtService.generateToken("guest_001", "00000000000", "guest");
        return Map.of(
                "tempToken", token,
                "expireAt", OffsetDateTime.now().plusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }
}
