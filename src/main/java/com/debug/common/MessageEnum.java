package com.debug.common;

import lombok.Getter;

@Getter
public enum MessageEnum {

    OK("요청 성공"),
    CREATED("생성 작업 성공"),
    UNMATCHED_ID_OR_PASSWORD("아이디 또는 비밀번호가 일치하지 않습니다.");

    final String message;

    MessageEnum(String message) {
        this.message = message;
    }
}
