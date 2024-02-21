package com.authmodule.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final HttpStatus httpStatus;
    public AppException(String message, HttpStatus _httpStatus) {
        super(message);
        this.httpStatus = _httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
