package com.debug.oauth.handler;

import com.debug.common.StatusEnum;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.HeaderUtil;
import com.debug.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenAccessDeniedHandler implements AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final AuthTokenProvider authTokenProvider;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException {

        AuthToken authToken = authTokenProvider.convertAuthToken(HeaderUtil.getAccessToken(request));
        Authentication authentication = authTokenProvider.getAuthentication(authToken);

        // TODO 2022.08.09 Role에 따라서 분기 처리를 해야함

        ResponseUtil.setResponse(response, StatusEnum.NO_PERMISSION);
        handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
    }
}
