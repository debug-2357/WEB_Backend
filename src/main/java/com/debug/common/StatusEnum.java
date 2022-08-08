package com.debug.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum StatusEnum {

    // 200
    GET_USER_INFO(OK, "user 정보를 반환했습니다."),

    // 201
    CREATE_TOKENS(CREATED, "access token과 refresh token을 생성 했습니다."),

    // 400
    NOT_EXPIRED_TOKEN_YET(BAD_REQUEST, "토큰이 아직 만료되지 않았습니다."),
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "잘못된 access token입니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "잘못된 refresh token입니다."),

    // 401
    UNMATCHED_ID_OR_PASSWORD(UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

    // 404,
    REFRESH_TOKEN_NOT_FOUND(NOT_FOUND, "refresh token가 존재하지 않습니다."),
    USER_NOT_FOUND(NOT_FOUND, "user가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String detail;

    StatusEnum(HttpStatus httpStatus, String detail) {
        this.httpStatus = httpStatus;
        this.detail = detail;
    }
}
