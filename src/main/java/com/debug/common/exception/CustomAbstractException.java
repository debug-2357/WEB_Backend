package com.debug.common.exception;

import com.debug.common.StatusEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class CustomAbstractException extends RuntimeException {
    protected final StatusEnum statusEnum;
}
