package com.debug.docs.common;

import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

public class CustomPreprocessors {
    public static OperationPreprocessor maskJwt() {
        return new JwtMaskingPreProcessor();
    }

    public static OperationPreprocessor maskJwt(String description) {
        return new JwtMaskingPreProcessor(description);
    }
}
