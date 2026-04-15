package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.config.AlipayConfig;
import com.neighbor.dto.CreatePaymentRequest;
import com.neighbor.dto.PaymentDTO;
import com.neighbor.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayConfig alipayConfig;

    public PaymentController(PaymentService paymentService, AlipayConfig alipayConfig) {
        this.paymentService = paymentService;
        this.alipayConfig = alipayConfig;
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createPayment(
            Authentication authentication,
            @RequestBody CreatePaymentRequest request) {
        Long payerId = getUserIdFromAuth(authentication);
        String formHtml = paymentService.createPayment(request.borrowId(), payerId);
        return ApiResponse.ok(Map.of("paymentForm", formHtml));
    }

    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        paymentService.handleNotify(params);
        return "success";
    }

    @GetMapping("/return")
    public void handleReturn(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = extractParams(request);
        boolean verified = paymentService.verifyReturn(params);
        String outTradeNo = params.get("out_trade_no");

        if (verified) {
            response.sendRedirect(alipayConfig.getReturnUrl() + "?out_trade_no=" + outTradeNo + "&status=success");
        } else {
            response.sendRedirect(alipayConfig.getReturnUrl() + "?status=fail");
        }
    }

    @GetMapping("/borrow/{borrowId}")
    public ApiResponse<PaymentDTO> getPaymentByBorrowId(@PathVariable Long borrowId) {
        PaymentDTO dto = paymentService.getPaymentByBorrowId(borrowId);
        return ApiResponse.ok(dto);
    }

    @GetMapping("/order/{outTradeNo}")
    public ApiResponse<PaymentDTO> getPaymentByOutTradeNo(@PathVariable String outTradeNo) {
        return ApiResponse.ok(paymentService.getPaymentByOutTradeNo(outTradeNo));
    }

    @GetMapping("/my/paid")
    public ApiResponse<List<PaymentDTO>> getMyPayments(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(paymentService.getMyPayments(userId));
    }

    @GetMapping("/my/received")
    public ApiResponse<List<PaymentDTO>> getReceivedPayments(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ApiResponse.ok(paymentService.getReceivedPayments(userId));
    }

    @PostMapping("/{borrowId}/refund")
    public ApiResponse<Map<String, String>> refundDeposit(
            Authentication authentication,
            @PathVariable Long borrowId) {
        Long operatorId = getUserIdFromAuth(authentication);
        paymentService.refundDeposit(borrowId, operatorId);
        return ApiResponse.ok(Map.of("status", "REFUNDED"));
    }

    @PostMapping("/{borrowId}/skip-pay")
    public ApiResponse<Map<String, String>> skipPayment(
            Authentication authentication,
            @PathVariable Long borrowId) {
        Long payerId = getUserIdFromAuth(authentication);
        paymentService.skipPayment(borrowId, payerId);
        return ApiResponse.ok(Map.of("status", "PAID"));
    }

    @PostMapping("/{borrowId}/skip-refund")
    public ApiResponse<Map<String, String>> skipRefund(
            Authentication authentication,
            @PathVariable Long borrowId) {
        Long operatorId = getUserIdFromAuth(authentication);
        paymentService.skipRefund(borrowId, operatorId);
        return ApiResponse.ok(Map.of("status", "REFUNDED"));
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }
        return params;
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未认证");
        }
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return Long.parseLong(authUser.userId());
    }
}
