package com.debug.docs.common;

import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

public class CustomPreprocessors {
    public static OperationPreprocessor maskJwtFromHeader() {
        return new HeaderJwtMaskingPreProcessor();
    }

    public static OperationPreprocessor maskJwtFromHeader(String description) {
        return new HeaderJwtMaskingPreProcessor(description);
    }
}
