package com.neighbor.common.exception;

import com.neighbor.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ApiResponse.fail(2001, msg);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        return ApiResponse.fail(2001, "参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        return ApiResponse.fail(5000, "系统异常");
    }
}
