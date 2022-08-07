package com.debug.common;

import lombok.Getter;

@Getter
public enum MessageEnum {

    OK("요청 성공"),
    CREATED("생성 작업 성공"),
    CREATE_ACCESS_TOKEN("access token을 생성 했습니다."),
    UNMATCHED_ID_OR_PASSWORD("아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_ACCESS_TOKEN("잘못된 access token입니다."),
    INVALID_REFRESH_TOKEN("잘못된 refresh token입니다."),
    NOT_EXPIRED_TOKEN_YET("토큰이 아직 만료되지 않았습니다.");

    final String message;

    MessageEnum(String message) {
        this.message = message;
    }
}
