package com.debug.oauth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum RoleType implements GrantedAuthority {
    CONFIRM(Code.confirm, "인증된 사용자 권한"),
    UNCONFIRMED(Code.unConfirmed, "인증 되지 않은 사용자 권한"),
    ADMIN(Code.admin, "관리자 권한"),
    GUEST(Code.guest, "게스트 권한");

    private final String authority;
    private final String displayName;

    private static final Map<String, RoleType> codes =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(RoleType::getAuthority, Function.identity())));

    public static RoleType of(String code) {
        return Optional.ofNullable(codes.get(code)).orElse(GUEST);
    }

    public static class Code {
        public static final String confirm = "ROLE_CONFIRM";
        public static final String unConfirmed = "ROLE_UNCONFIRMED";
        public static final String admin = "ROLE_ADMIN";
        public static final String guest = "ROLE_GUEST";
    }
}
