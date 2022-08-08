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

        authException.printStackTrace();
        log.info("Responding with unauthorized error. Message := {}", authException.getMessage());

        // 토큰이 잘못 되어있다면
        if (!token.validate()) {
            ResponseUtil.setResponse(response, StatusEnum.INVALID_ACCESS_TOKEN);
        // 토큰이 만료됬다면
        } else if (token.getExpiredTokenClaims() != null) {
            ResponseUtil.setResponse(response, StatusEnum.EXPIRED_ACCESS_TOKEN);
        }
    }
}
