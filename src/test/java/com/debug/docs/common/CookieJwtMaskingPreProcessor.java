package com.debug.docs.common;

import org.springframework.restdocs.operation.*;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

import java.util.ArrayList;
import java.util.Collection;

public class CookieJwtMaskingPreProcessor implements OperationPreprocessor {

    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private final String refreshTokenValue;

    public CookieJwtMaskingPreProcessor() {
        this.refreshTokenValue = "{refresh token}";
    }

    public CookieJwtMaskingPreProcessor(String description) {
        this.refreshTokenValue = description;
    }

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        Collection<RequestCookie> oldCookie = request.getCookies();
        Collection<RequestCookie> newCookie = new ArrayList<>();

        oldCookie.forEach(cookie -> {
            if (!cookie.getName().equals(REFRESH_TOKEN_NAME)) {
                newCookie.add(cookie);
            } else {
                RequestCookie refreshTokenCookie = new RequestCookie(REFRESH_TOKEN_NAME, refreshTokenValue);
                newCookie.add(refreshTokenCookie);
            }
        });

        return new OperationRequestFactory().create(request.getUri(),
                request.getMethod(), request.getContent(), request.getHeaders(),
                request.getParameters(), request.getParts(), newCookie);
    }

    @Override
    public OperationResponse preprocess(OperationResponse response) {
        return null;
    }
}
