package com.debug.oauth.exception;

import com.debug.common.StatusEnum;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.HeaderUtil;
import com.debug.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthTokenProvider authTokenProvider;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String tokenString = HeaderUtil.getAccessToken(request);
        AuthToken token = authTokenProvider.convertAuthToken(tokenString);

        log.info("Responding with unauthorized error. Message := {}", authException.getMessage());

        // 토큰이 만료되었으면
        try {
            if (token.getExpiredTokenClaims() != null) {
                ResponseUtil.setResponse(response, StatusEnum.EXPIRED_ACCESS_TOKEN);
            }
        // 그외 예외인 경우 jwt 토큰이 잘못됨
        } catch (Exception e) {
            ResponseUtil.setResponse(response, StatusEnum.INVALID_ACCESS_TOKEN);
        }
    }
}
