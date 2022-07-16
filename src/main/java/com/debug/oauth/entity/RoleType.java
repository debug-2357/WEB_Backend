package com.debug.oauth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum RoleType {
    CONFIRM("ROLE_CONFIRM", "인증된 사용자 권한"),
    UNCONFIRMED("ROLE_UNCONFIRMED", "인증 되지 않은 사용자 권한"),
    ADMIN("ROLE_ADMIN", "관리자 권한"),
    GUEST("GUEST", "게스트 권한");

    private final String code;
    private final String displayName;

    private static final Map<String, RoleType> codes =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(RoleType::getCode, Function.identity())));

    public static RoleType of(String code) {
        return Optional.ofNullable(codes.get(code)).orElse(GUEST);
    }
}
