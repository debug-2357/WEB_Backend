package com.debug.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum StatusEnum {

    // 200
    GET_USER_INFO(OK, "user 정보를 반환했습니다."),
    USER_EXISTS(OK, "해당 유저는 db에 존재합니다."),
    SUCCESS_SIGN_UP(OK, "회원가입을 성공적으로 마쳤습니다."),
    GET_RECRUIT_PERIOD(OK, "지원기간 정보를 반환 했습니다."),
    GET_RECRUIT_APPLY(OK, "지원서 정보를 반환 했습니다"),
    GET_RECENT_RECRUIT_PERIOD(OK, "최근 지원기간 정보를 반환 했습니다."),
    UPDATE_RECRUIT_APPLY(OK, "지원서를 성공적으로 수정했습니다."),
    DELETE_RECRUIT_APPLY(OK, "지원서를 성공적으로 삭제했습니다."),

    // 201
    CREATE_TOKENS(CREATED, "access token과 refresh token을 생성 했습니다."),
    CREATE_USER(CREATED, "user를 생성했습니다."),
    CREATE_RECRUIT_APPLY(CREATED, "지원서를 생성했습니다."),

    // 204
    USER_DOSE_NOT_EXIST(NO_CONTENT, "해당 유저의 id는 db에 없습니다."),
    NO_RECRUIT_PERIOD_CONTENT(NO_CONTENT, "지원 기간들의 정보가 없습니다."),
    NO_RECRUIT_APPLY_CONTENT(NO_CONTENT, "지원서들의 정보가 없습니다"),

    // 400
    NOT_EXPIRED_ACCESS_TOKEN_YET(BAD_REQUEST, "access token이 아직 만료되지 않았습니다."),
    INVALID_PASSWORD(BAD_REQUEST, "password1과 password2가 틀립니다."),
    USERID_ALREADY_IN_USER(BAD_REQUEST, "해당 유저가 이미 존재합니다"),
    NON_RECEPTION_PERIOD(BAD_REQUEST, "지원 기간이 아닙니다."),
    ALREADY_APPLIED_BY_USER(BAD_REQUEST, "해당 유저는 이미 지원했습니다."),

    // 401
    UNMATCHED_ID_OR_PASSWORD(UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_TOKEN_IS_NULL(UNAUTHORIZED, "access_token이 존재하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(UNAUTHORIZED, "access token이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(UNAUTHORIZED, "잘못된 access token입니다."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "잘못된 refresh token입니다."),

    // 403
    NEED_SIGN_UP(FORBIDDEN, "요청한 사용자는 회원가입을 마쳐야합니다."),
    NO_PERMISSION(FORBIDDEN, "요청한 사용자는 권한이 없습니다."),
    NO_RECRUIT_APPLY_PERMISSION(FORBIDDEN, "요청한 사용자와 지원 작성 사용자가 다릅니다."),

    // 404,
    REFRESH_TOKEN_NOT_FOUND(NOT_FOUND, "refresh token가 존재하지 않습니다."),
    USER_NOT_FOUND(NOT_FOUND, "user가 존재하지 않습니다."),
    RECRUIT_PERIOD_NOT_FOUND(NOT_FOUND, "지원 기간이 존재하지 않습니다."),
    RECRUIT_APPLY_NOT_FOUND(NOT_FOUND, "지원서가 존재하지 않습니다");

    private final HttpStatus httpStatus;
    private final String detail;

    StatusEnum(HttpStatus httpStatus, String detail) {
        this.httpStatus = httpStatus;
        this.detail = detail;
    }
}
