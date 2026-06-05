package com.restaurant.common;

/**
 * 業務邏輯例外
 * 例如：訂位人數超過容量、點數不足、門市不存在等
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
