package com.debug.common;

public enum MessageEnum {

    OK("요청 성공"),
    CREATED("생성 작업 성공");

    final String message;

    MessageEnum(String message) {
        this.message = message;
    }
}
