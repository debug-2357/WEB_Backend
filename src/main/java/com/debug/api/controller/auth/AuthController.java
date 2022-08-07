package com.debug.api.controller.auth;

import com.debug.api.dto.request.AuthRequest;
import com.debug.api.entity.user.UserRefreshToken;
import com.debug.api.repository.user.UserRefreshTokenRepository;
import com.debug.common.ApiBody;
import com.debug.common.MessageEnum;
import com.debug.config.properties.AppProperties;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.entity.UserPrincipal;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.CookieUtil;
import com.debug.util.HeaderUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    private final static long THREE_DAYS_MSEC = 259200000;
    private final static String REFRESH_TOKEN = "refresh_token";

    @PostMapping("/login")
    public ResponseEntity<ApiBody> login(HttpServletRequest request,
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
            ApiBody apiBody = ApiBody.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message(MessageEnum.UNMATCHED_ID_OR_PASSWORD)
                    .data("")
                    .build();

            // 검증에 실패하면 로그인 실패 response 반환
            return new ResponseEntity<>(apiBody, HttpStatus.UNAUTHORIZED);
        }

        // 검증이 된다면 security 내 Authentication을 설정
        String userId = authRequest.getId();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 액세스 토큰 설정
        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userId,
                ((UserPrincipal) authentication.getPrincipal()).getRoleType().getAuthority(),
                new Date(now.getTime() + 1)
        );

        // 리프래쉬 토큰 설정
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );

        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUserId(userId);
        if (userRefreshToken != null) {
            // 리프래쉬 토큰이 db에 존재하면 업데이트
            userRefreshToken.updateRefreshToken(refreshToken.getToken());
        } else {
            // db에 존재하지않으면 db에 저장
            userRefreshToken = UserRefreshToken.builder()
                    .userId(userId)
                    .refreshToken(refreshToken.getToken())
                    .build();
        }
        userRefreshTokenRepository.saveAndFlush(userRefreshToken);

        // 쿠키에 리프래쉬 토큰 설정
        int cookieMaxAge = (int) refreshTokenExpiry / 60;

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, userRefreshToken.getRefreshToken(), cookieMaxAge);

        ApiBody apiBody = ApiBody.builder()
                .status(HttpStatus.OK)
                .message(MessageEnum.CREATE_ACCESS_TOKEN)
                .data(Map.of("access_token", accessToken.getToken()))
                .build();

        // body에 액세스 토큰, 쿠키에 리프래쉬 토큰을 설정하고 보냄
        return new ResponseEntity<>(apiBody, HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiBody> refreshToken(HttpServletRequest request,
                                                HttpServletResponse response) {
        String accessToken = HeaderUtil.getAccessToken(request);
        AuthToken authAccessToken = tokenProvider.convertAuthToken(accessToken);

        // TODO 2020.08.07 refresh 할 때 access token도 필요성이 있는지 고려해야함
        // access token이 유효하지 않으면 401 에러 반환
        if (!authAccessToken.validate()) {
            ApiBody apiBody = ApiBody.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message(MessageEnum.INVALID_ACCESS_TOKEN)
                    .data("")
                    .build();
            return new ResponseEntity<>(apiBody, HttpStatus.UNAUTHORIZED);
        }

        Claims claims = authAccessToken.getExpiredTokenClaims();
        // access token이 만료되지 않았으면 403 에러 반환
        if (claims == null) {
            ApiBody apiBody = ApiBody.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message(MessageEnum.NOT_EXPIRED_TOKEN_YET)
                    .data("")
                    .build();
            return new ResponseEntity<>(apiBody, HttpStatus.FORBIDDEN);
        }

        String userId = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get("role", String.class));

        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse(null);
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        // refresh token이 유효하지 않으면 401 에러 반환
        if (!authRefreshToken.validate()) {
            ApiBody apiBody = ApiBody.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message(MessageEnum.INVALID_REFRESH_TOKEN)
                    .data("")
                    .build();
            return new ResponseEntity<>(apiBody, HttpStatus.UNAUTHORIZED);
        }

        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken);
        // refresh token이 db에 존재하지 않으면 401 에러 반환
        if (userRefreshToken == null) {
            ApiBody apiBody = ApiBody.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message(MessageEnum.INVALID_REFRESH_TOKEN)
                    .data("")
                    .build();
            return new ResponseEntity<>(apiBody, HttpStatus.UNAUTHORIZED);
        }

        Date now = new Date();
        AuthToken newAccessToken = tokenProvider.createAuthToken(
                userId,
                roleType.getAuthority(),
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

        ApiBody apiBody = ApiBody.builder()
                .status(HttpStatus.OK)
                .message(MessageEnum.CREATE_ACCESS_TOKEN)
                .data(Map.of("access_token", newAccessToken.getToken()))
                .build();

        return new ResponseEntity<>(apiBody, HttpStatus.OK);
    }
}
