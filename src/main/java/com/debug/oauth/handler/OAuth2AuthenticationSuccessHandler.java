package com.debug.oauth.handler;

import com.debug.api.service.user.UserRefreshTokenService;
import com.debug.config.properties.AppProperties;
import com.debug.oauth.entity.ProviderType;
import com.debug.oauth.info.OAuth2UserInfo;
import com.debug.oauth.info.OAuth2UserInfoFactory;
import com.debug.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import static com.debug.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URL_PARAM_COOKIE_NAME;
import static com.debug.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository.REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final UserRefreshTokenService userRefreshTokenService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;

    // 성공할 경우 메소드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        // determineTargetUrl 메소드를 이용해 클라이언트 uri를 구성
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        // request, session에 존재하는 인증 정보들 삭제
        clearAuthenticationAttributes(request, response);
        // 인증 정보들을 포함해서 targetUrl로 리다이렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 클라이언트 uri를 param에 토큰을 포함해서 uri를 구성하는 메소드
     * @param request request
     * @param response response
     * @param authentication 인증
     * @return uri
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URL_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        // redirectUri가 값이 있고 프로퍼티에 지정한 클라이언트 uri과 다를경우 예외처리
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with authentication");
        }

        // redirectUri가 null이 아니면 값을 가져오고 null이면 기본 uri(서버 uri)를 targetUrl에 저장한다.
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());

        OidcUser user = (OidcUser) authentication.getPrincipal();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, user.getAttributes());
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        String authority = getAuthority(authorities);

        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userInfo.getId(),
                authority,
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );

        userRefreshTokenService.save(refreshToken.getToken(), userInfo.getId());

        int cookieMaxAge = (int) refreshTokenExpiry / 60;

        // 쿠키에 리프레쉬 토큰 저장
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge);

        // targetUrl을 바탕, param인 token을 포함하고 url을 구성해 반환
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken.getToken())
                .build()
                .toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        // 세션에 인증 정보 삭제
        super.clearAuthenticationAttributes(request);
        // request 쿠키에 있는 부가적인 정보를 삭제한다.
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        if (authorities == null) {
            return false;
        }

        for (GrantedAuthority grantedAuthority : authorities) {
            if (authorities.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    private String getAuthority(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return null;
        }

        for (GrantedAuthority grantedAuthority : authorities) {
            return grantedAuthority.getAuthority();
        }

        return null;
    }

    /**
     * appProperties에 지정한 클라이언트 uri과 요청된 uri의 서버와 포트가 같은지 비교
     * @param uri request uri
     * @return boolean
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOAuth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
