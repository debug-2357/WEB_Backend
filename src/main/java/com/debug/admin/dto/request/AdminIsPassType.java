package com.debug.admin.dto.request;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum AdminIsPassType {
    PASS(true, "합격"),
    FAIL(false, "불합격"),
    REVIEW(null, "검토중");

    private final Boolean isPass;
    private final String description;

    AdminIsPassType(Boolean isPass, String description) {
        this.isPass = isPass;
        this.description = description;
    }

    public static final Map<String, AdminIsPassType> enums =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(AdminIsPassType::getDescription, Function.identity())));

    public static AdminIsPassType of(String requestString) {
        return Optional.of(enums.get(requestString)).orElse(REVIEW);
    }
}
