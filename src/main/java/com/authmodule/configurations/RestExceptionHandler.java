package com.authmodule.configurations;

import com.authmodule.dto.ErrorResponseDto;
import com.authmodule.exceptions.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(value = { AppException.class })
    @ResponseBody
    public ResponseEntity<ErrorResponseDto>handleException(AppException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponseDto(e.getMessage()));
    }
}
