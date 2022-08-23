package com.debug.config.security;

import com.debug.api.repository.user.UserRefreshTokenRepository;
import com.debug.api.service.user.UserRefreshTokenService;
import com.debug.config.properties.AppProperties;
import com.debug.config.properties.CorsProperties;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.exception.RestAuthenticationEntryPoint;
import com.debug.oauth.filter.TokenAuthenticationFilter;
import com.debug.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.debug.oauth.handler.OAuth2AuthenticationSuccessHandler;
import com.debug.oauth.handler.TokenAccessDeniedHandler;
import com.debug.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.debug.oauth.service.CustomOAuth2UserService;
import com.debug.oauth.service.CustomUserDetailsService;
import com.debug.oauth.token.AuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final AppProperties appProperties;
    private final AuthTokenProvider authTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenAccessDeniedHandler tokenAccessDeniedHandler;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRefreshTokenService userRefreshTokenService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(authTokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(
                authTokenProvider,
                appProperties,
                userRefreshTokenService,
                oAuth2AuthorizationRequestBasedOnCookieRepository()
        );
    }

    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(oAuth2AuthorizationRequestBasedOnCookieRepository());
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedHeaders(List.of(corsProperties.getAllowedHeaders()));
        corsConfig.setAllowedMethods(List.of(corsProperties.getAllowedMethods()));
        corsConfig.setAllowedOrigins(List.of(corsProperties.getAllowedOrigins()));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(corsConfig.getMaxAge());

        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                    .cors()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .csrf().disable()
                    .formLogin().disable()
                    .httpBasic().disable()
                    .exceptionHandling()
                    .authenticationEntryPoint(new RestAuthenticationEntryPoint(authTokenProvider))
                    .accessDeniedHandler(tokenAccessDeniedHandler)
                .and()
                    // TODO 2022.08.01 uri 마다 권한 설정 해줘야함 - 현수
                    .authorizeRequests()
                    .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
//                    .antMatchers("/", "/oauth2/**", "/api/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users")
                    .hasAnyAuthority(
                            RoleType.ADMIN.getAuthority(),
                            RoleType.UNCONFIRMED.getAuthority(),
                            RoleType.CONFIRM.getAuthority()
                            )
                .antMatchers(HttpMethod.PATCH, "/api/users").hasAuthority(RoleType.GUEST.getAuthority())
                .anyRequest().permitAll()
//                    .antMatchers("/api/**").hasAnyAuthority(RoleType.UNCONFIRMED.getCode())
//                    .antMatchers("/api/**/admin/**").hasAnyAuthority(RoleType.ADMIN.getAuthority())
//                    .anyRequest().authenticated()
                .and()
                    .oauth2Login()
                    .authorizationEndpoint()
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                .and()
                    .redirectionEndpoint()
                    .baseUri("/*/oauth2/code/*")
                .and()
                    .userInfoEndpoint()
                    .userService(oAuth2UserService)
                .and()
                    // TODO 2020.08.18 oauth2 성공 또는 실패시 response로 토큰 반환
                    .successHandler(oAuth2AuthenticationSuccessHandler())
                    .failureHandler(oAuth2AuthenticationFailureHandler());

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
