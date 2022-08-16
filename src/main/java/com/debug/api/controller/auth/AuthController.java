package com.debug.api.controller.auth;

import com.debug.api.dto.request.AuthRequest;
import com.debug.api.entity.user.User;
import com.debug.api.entity.user.UserRefreshToken;
import com.debug.api.exception.InvalidAccessTokenException;
import com.debug.api.exception.InvalidRefreshTokenException;
import com.debug.api.exception.LoginFailedException;
import com.debug.api.exception.UnexpiredAccessTokenException;
import com.debug.api.service.UserRefreshTokenService;
import com.debug.common.StatusEnum;
import com.debug.common.response.SuccessResponseBody;
import com.debug.config.properties.AppProperties;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.entity.UserPrincipal;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.CookieUtil;
import com.debug.util.HeaderUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppProperties appProperties;
    private final AuthTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRefreshTokenService userRefreshTokenService;
    private final static long THREE_DAYS_MSEC = 259200000;
    private final static String REFRESH_TOKEN = "refresh_token";

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseBody> login(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @RequestBody AuthRequest authRequest) {

        Authentication authentication;
        // 로그인 검증
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getId(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // 검증에 실패하면 로그인 실패 response 반환
            throw new LoginFailedException();
        }

        // 검증이 된다면 security 내 Authentication을 설정
        String userId = authRequest.getId();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 액세스 토큰 설정
        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userId,
                ((UserPrincipal) authentication.getPrincipal()).getRoleType().getAuthority(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        // 리프래쉬 토큰 설정
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );
        userRefreshTokenService.save(refreshToken.getToken(), userId);

        // 쿠키에 리프래쉬 토큰 설정
        int cookieMaxAge = (int) refreshTokenExpiry / 60;

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge);

        // body에 액세스 토큰, 쿠키에 리프래쉬 토큰을 설정하고 보냄
        return SuccessResponseBody.toResponseEntity(
                StatusEnum.CREATE_TOKENS,
                Map.of("access_token", accessToken.getToken())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponseBody> refreshToken(HttpServletRequest request,
                                                HttpServletResponse response) {
        String accessToken = HeaderUtil.getAccessToken(request);
        AuthToken authAccessToken = tokenProvider.convertAuthToken(accessToken);

        // access token이 유효하지 않으면 400 에러 반환
        if (!authAccessToken.validate()) {
            throw new InvalidAccessTokenException();
        }

        Claims claims = authAccessToken.getExpiredTokenClaims();
        // access token이 만료되지 않았으면 400 에러 반환
        if (claims == null) {
            throw new UnexpiredAccessTokenException();
        }

        String userId = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get("role", String.class));

        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse(null);
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        // refresh token이 유효하지 않으면 400 에러 반환
        if (!authRefreshToken.validate()) {
            throw new InvalidRefreshTokenException();
        }

        // refresh token이 db에 존재하지 않으면 404 에러 반환
        UserRefreshToken userRefreshToken = userRefreshTokenService.findByRefreshTokenAndUserId(refreshToken, userId);

        User user = userRefreshToken.getUser();

        Date now = new Date();
        AuthToken newAccessToken = tokenProvider.createAuthToken(
                user.getUserId(),
                user.getRoleType().getAuthority(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

        // refresh token 유효기간이 3일 이하면 새로운 토큰을 생성하고 db, 쿠키에 최신화한다.
        if (validTime <= THREE_DAYS_MSEC) {
            long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

            AuthToken newAuthRefreshToken = tokenProvider.createAuthToken(
                    appProperties.getAuth().getTokenSecret(),
                    new Date(now.getTime() + refreshTokenExpiry)
            );

            userRefreshToken.updateRefreshToken(newAuthRefreshToken.getToken());

            int cookieMaxAge = (int) refreshTokenExpiry / 60;
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
            CookieUtil.addCookie(response, REFRESH_TOKEN, newAuthRefreshToken.getToken(), cookieMaxAge);
        }

        return SuccessResponseBody.toResponseEntity(
                StatusEnum.CREATE_TOKENS,
                Map.of("access_token", newAccessToken.getToken())
        );
    }
}
