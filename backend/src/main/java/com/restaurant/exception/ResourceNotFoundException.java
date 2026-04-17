package com.restaurant.exception;

/**
 * 資源不存在例外
 * 例如：查詢的門市 ID 不存在、會員 ID 不存在等
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " 不存在，ID: " + id);
    }
}
