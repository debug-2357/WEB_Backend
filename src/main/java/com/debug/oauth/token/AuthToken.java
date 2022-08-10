package com.debug.oauth.token;

import com.google.gson.JsonSyntaxException;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class AuthToken {

    @Getter
    private final String token;
    private final Key key;

    private static final String AUTHORITIES_KEY = "role";

    public AuthToken(String id, Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(id, expiry);
    }

    public AuthToken(String id, String role, Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(id, role, expiry);
    }

    /**
     * user id를 포함한 jwt 토큰 반환
     * @param id userid
     * @param expiry 만료 시간
     * @return jwt token
     */
    private String createAuthToken(String id, Date expiry) {
        return Jwts.builder()
                .setSubject(id)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    /**
     * user id와 해당 user의 role을 포함한 jwt 토큰 반환
     * @param id userid
     * @param role user의 role
     * @param expiry 만료 시간
     * @return jwt token
     */
    private String createAuthToken(String id, String role, Date expiry) {
        return Jwts.builder()
                .setSubject(id)
                .claim(AUTHORITIES_KEY, role)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    /**
     * 이 클래스의 jwt token이 유효한지 검증
     * @return true or false
     */
    public boolean validate() {
        return this.getTokenClaims() != null;
    }

    /**
     * jwt token에 속한 claims(속성 정보)을 반환
     * @return Claims or null
     */
    public Claims getTokenClaims() {
        try {
            return (Claims) Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parse(token)
                    .getBody();
        // 토큰이 기존 서명을 확인하지 못할 경우
        } catch (SecurityException e) {
            log.info("Invalid JWT signature.");
        // 토큰에 들어온 토큰값이 올바르지 않을 경우
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
        // jwt 토큰이 만료 된 경우
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            return e.getClaims();
        // 지원되지 않는 jwt 토큰일 경우
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token");
        // 잘못된 jwt 토큰일 경우
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler and invalid.");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.info("JWT signature does not match locally computed signature : {}", token);
        } catch (JsonSyntaxException e) {
            log.info("Gson attempts to read (or write) a malformed JSON element.");
        }
        return null;
    }

    /**
     * 만료된 token의 claims을 반환
     * @return Claims or null
     */
    public Claims getExpiredTokenClaims() {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token/");
            return e.getClaims();
        }
        return null;
    }
}
