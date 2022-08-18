package com.debug.docs.common;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

public class JwtMaskingPreProcessor implements OperationPreprocessor {

    private String headerValue = "Bearer ";
    private final static String HEADER_NAME = "Authorization";

    public JwtMaskingPreProcessor() {
        this.headerValue += "XXX";
    }

    public JwtMaskingPreProcessor(String description) {
        this.headerValue += description;
    }

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        HttpHeaders oldHeader = request.getHeaders();
        HttpHeaders newHeader = new HttpHeaders();

        oldHeader.forEach((name, value) -> {
            if (!name.equals(HEADER_NAME)) {
                newHeader.addAll(name, value);
            } else {
                newHeader.add(HEADER_NAME, headerValue);
            }
        });

        return new OperationRequestFactory().create(request.getUri(),
                request.getMethod(), request.getContent(), newHeader,
                request.getParameters(), request.getParts());
    }

    @Override
    public OperationResponse preprocess(OperationResponse response) {
        return null;
    }
}
