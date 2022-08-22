package com.debug.docs.auth;

import com.debug.api.controller.auth.AuthController;
import com.debug.api.dto.request.AuthRequest;
import com.debug.api.dto.request.RegisterRequest;
import com.debug.api.entity.user.User;
import com.debug.api.entity.user.UserRefreshToken;
import com.debug.api.exception.LoginFailedException;
import com.debug.api.repository.user.UserRepository;
import com.debug.api.service.UserRefreshTokenService;
import com.debug.config.properties.AppProperties;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.entity.UserPrincipal;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import com.debug.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.security.Key;
import java.util.Date;

import static com.debug.docs.common.CustomPreprocessors.maskJwtFromCookie;
import static com.debug.docs.common.CustomPreprocessors.maskJwtFromHeader;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("development")
@WebMvcTest(AuthController.class)
@Import({AppProperties.class})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class AuthControllerTests {

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    @MockBean
    private AuthTokenProvider authTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private Authentication authentication;

    @MockBean
    private UserRefreshTokenService userRefreshTokenService;

    private static long refreshTokenExpiry;
    private static final long THREE_DAYS_MSEC = 259200000;

    private final static String NORMAL_ACCESS_TOKEN = "{정상적인 access token}";
    private final static String INVALID_ACCESS_TOKEN = "{위조 또는 잘못된 access token}";
    private final static String EXPIRED_ACCESS_TOKEN = "{만료된 access token}";
    private final static String NORMAL_REFRESH_TOKEN = "{정상적인 refresh token}";
    private final static String INVALID_REFRESH_TOKEN = "{위조 또는 잘못된 refresh token}";
    private final static String EXPIRED_LESS_THAN_THREE_DAYS_REFRESH_TOKEN = "{만료 시간이 3일 이하인 refresh token}";

    @BeforeEach
    void setUp() {
        refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();
    }

    private Key getTestKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private UserRefreshToken getTestUserRefreshTokenEntity(User user, String token) {
        return UserRefreshToken.builder()
                .user(user)
                .refreshToken(token)
                .build();
    }

    private User getTestUser() {
        return User.builder()
                .userId("test")
                .OAuth2UserId("")
                .username("test_name")
                .password(new BCryptPasswordEncoder().encode("password"))
                .email("test@gmail.com")
                .emailVerifiedYn("Y")
                .profileImageUrl("")
                .providerType(null)
                .roleType(RoleType.GUEST)
                .build();
    }

    private UserPrincipal getTestPrincipal() {
        return UserPrincipal.create(getTestUser());
    }


    @Test
    @DisplayName("로그인 성공")
    public void success_login() throws Exception {
        AuthRequest authRequest = AuthRequest.builder()
                .id("test")
                .password("password")
                .build();

        AuthToken accessToken = new AuthToken("{access token}", getTestKey());
        AuthToken refreshToken = new AuthToken("{refresh token}", getTestKey());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(getTestPrincipal());
        when(authTokenProvider.createAuthToken(anyString(), anyString(), any()))
                .thenReturn(accessToken);
        when(authTokenProvider.createAuthToken(anyString(), any()))
                .thenReturn(refreshToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest))
        );

        perform.andExpect(status().isCreated())
                .andDo(document("auth/success-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("id").description("로그인 할 아이디"),
                                fieldWithPath("password").description("로그인 할 패스워드")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.access_token").description("인증에 필요한 jwt 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 정보를 찾을 수 없어 로그인 실패")
    public void failure_login_when_not_found_user() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .userId("test")
                .username("test_username")
                .password1("password1")
                .password2("password1")
                .email("test@gmail.com")
                .build();

        AuthRequest authRequest = AuthRequest.builder()
                .id("test")
                .password("password")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new LoginFailedException());

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest))
        );

        perform.andExpect(status().isUnauthorized())
                .andDo(document("auth/failure-login-when-not-found-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("id").description("로그인 할 아이디"),
                                fieldWithPath("password").description("로그인 할 패스워드")
                        )
                ));
    }

    @Test
    @DisplayName("refresh token의 만료 시간이 3일 넘게 남을 경우의 refresh 성공")
    public void success_refresh_when_not_update_refresh_token() throws Exception {
        Date now = new Date();
        User user = getTestUser();

        // 만료된 접근 토큰
        AuthToken requestAccessToken = new AuthToken(
                user.getUserId(),
                user.getRoleType().getAuthority(),
                new Date(0),
                getTestKey()
        );

        // request refresh token의 잔여시간은 만료되지 않은 상태로 설정
        AuthToken requestRefreshToken = new AuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry),
                getTestKey()
        );
        Cookie requestCookie = new Cookie("refresh_token", requestRefreshToken.getToken());
        requestCookie.setPath("/");
        requestCookie.setHttpOnly(true);
        requestCookie.setMaxAge((int) (refreshTokenExpiry / 60));

        // 새로운 토큰
        AuthToken responseAccessToken = new AuthToken(
                NORMAL_ACCESS_TOKEN,
                getTestKey()
        );

        // request 검증
        when(authTokenProvider.convertAuthToken(requestAccessToken.getToken()))
                .thenReturn(requestAccessToken);
        when(authTokenProvider.convertAuthToken(requestRefreshToken.getToken()))
                .thenReturn(requestRefreshToken);
        when(userRefreshTokenService.findByRefreshTokenAndUserId(anyString(), anyString()))
                .thenReturn(getTestUserRefreshTokenEntity(user, requestRefreshToken.getToken()));

        // response setting
        when(authTokenProvider.createAuthToken(anyString(), anyString(), any()))
                .thenReturn(responseAccessToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + requestAccessToken.getToken())
                        .cookie(requestCookie)
        );

        perform.andExpect(status().isCreated())
                .andDo(document("auth/success-refresh-not-update-when-refresh-token",
                        preprocessRequest(prettyPrint(), maskJwtFromHeader(EXPIRED_ACCESS_TOKEN), maskJwtFromCookie(NORMAL_REFRESH_TOKEN)),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("access token")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.access_token").description("인증에 필요한 jwt 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("refresh token의 만료 시간이 3일 미만 일 경우의 refresh 성공")
    public void success_refresh_when_update_refresh_token() throws Exception {
        Date now = new Date();
        User user = getTestUser();

        // 만료된 접근 토큰
        AuthToken requestAccessToken = new AuthToken(
                user.getUserId(),
                user.getRoleType().getAuthority(),
                new Date(0),
                getTestKey()
        );

        // request refresh token의 잔여시간은 만료되지 않은 상태로 설정
        AuthToken requestRefreshToken = new AuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + THREE_DAYS_MSEC),
                getTestKey()
        );
        Cookie requestCookie = new Cookie("refresh_token", requestRefreshToken.getToken());
        requestCookie.setPath("/");
        requestCookie.setHttpOnly(true);
        requestCookie.setMaxAge((int) (THREE_DAYS_MSEC / 60));

        // 새로운 토큰
        AuthToken responseAccessToken = new AuthToken(
                NORMAL_ACCESS_TOKEN,
                getTestKey()
        );

        AuthToken responseRefreshToken = new AuthToken(
                NORMAL_REFRESH_TOKEN,
                getTestKey()
        );

        // request 검증
        when(authTokenProvider.convertAuthToken(requestAccessToken.getToken()))
                .thenReturn(requestAccessToken);
        when(authTokenProvider.convertAuthToken(requestRefreshToken.getToken()))
                .thenReturn(requestRefreshToken);
        when(userRefreshTokenService.findByRefreshTokenAndUserId(anyString(), anyString()))
                .thenReturn(getTestUserRefreshTokenEntity(user, requestRefreshToken.getToken()));

        // response setting
        when(authTokenProvider.createAuthToken(anyString(), anyString(), any()))
                .thenReturn(responseAccessToken);
        when(authTokenProvider.createAuthToken(anyString(), any()))
                .thenReturn(responseRefreshToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + requestAccessToken.getToken())
                        .cookie(requestCookie)
        );

        perform.andExpect(status().isCreated())
                .andDo(document("auth/success-refresh-when-update-refresh-token",
                        preprocessRequest(prettyPrint(), maskJwtFromHeader(EXPIRED_ACCESS_TOKEN), maskJwtFromCookie(EXPIRED_LESS_THAN_THREE_DAYS_REFRESH_TOKEN)),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("access token")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.access_token").description("인증에 필요한 jwt 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("access token이 유효하지 않은 경우의 refresh 실패")
    public void failure_refresh_when_invalid_access_token() throws Exception {
        Date now = new Date();

        // 유효하지 않은 access token
        AuthToken requestAccessToken = new AuthToken(
                INVALID_ACCESS_TOKEN,
                getTestKey()
        );

        AuthToken requestRefreshToken = new AuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry),
                getTestKey()
        );
        Cookie requestCookie = new Cookie("refresh_token", requestRefreshToken.getToken());
        requestCookie.setPath("/");
        requestCookie.setHttpOnly(true);
        requestCookie.setMaxAge((int) (THREE_DAYS_MSEC / 60));

        // request 검증
        when(authTokenProvider.convertAuthToken(requestAccessToken.getToken()))
                .thenReturn(requestAccessToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + requestAccessToken.getToken())
                        .cookie(requestCookie)
        );

        perform.andExpect(status().isUnauthorized())
                .andDo(document("auth/failure-refresh-when-invalid-access-token",
                        preprocessRequest(prettyPrint(), maskJwtFromHeader(INVALID_ACCESS_TOKEN), maskJwtFromCookie(NORMAL_REFRESH_TOKEN)),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("access token")
                        )
                ));
    }

    @Test
    @DisplayName("access token이 아직 만료되지 않을 경우의 refresh 실패")
    public void failure_refresh_when_not_expired_access_token() throws Exception {
        Date now = new Date();
        User user = getTestUser();

        // 만료 되지않은 정상적인 토큰
        AuthToken requestAccessToken = new AuthToken(
                user.getUserId(),
                user.getRoleType().getAuthority(),
                new Date(now.getTime() + 1000000L),
                getTestKey()
        );

        AuthToken requestRefreshToken = new AuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry),
                getTestKey()
        );
        Cookie requestCookie = new Cookie("refresh_token", requestRefreshToken.getToken());
        requestCookie.setPath("/");
        requestCookie.setHttpOnly(true);
        requestCookie.setMaxAge((int) (THREE_DAYS_MSEC / 60));

        // request 검증
        when(authTokenProvider.convertAuthToken(requestAccessToken.getToken()))
                .thenReturn(requestAccessToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + requestAccessToken.getToken())
                        .cookie(requestCookie)
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("auth/failure-refresh-when-not-expired-access-token",
                        preprocessRequest(prettyPrint(), maskJwtFromHeader(NORMAL_ACCESS_TOKEN), maskJwtFromCookie(NORMAL_REFRESH_TOKEN)),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("access token")
                        )
                ));
    }

    @Test
    @DisplayName("refresh token이 유효하지 않은 경우의 refresh 실패")
    public void failure_refresh_when_invalid_refresh_token() throws Exception {
        Date now = new Date();
        User user = getTestUser();

        // 만료된 접근 토큰
        AuthToken requestAccessToken = new AuthToken(
                user.getUserId(),
                user.getRoleType().getAuthority(),
                new Date(0),
                getTestKey()
        );

        // 잘못된 refresh token
        AuthToken requestRefreshToken = new AuthToken(
                INVALID_REFRESH_TOKEN,
                getTestKey()
        );
        Cookie requestCookie = new Cookie("refresh_token", requestRefreshToken.getToken());
        requestCookie.setPath("/");
        requestCookie.setHttpOnly(true);
        requestCookie.setMaxAge((int) (THREE_DAYS_MSEC / 60));

        // request 검증
        when(authTokenProvider.convertAuthToken(requestAccessToken.getToken()))
                .thenReturn(requestAccessToken);
        when(authTokenProvider.convertAuthToken(requestRefreshToken.getToken()))
                .thenReturn(requestRefreshToken);

        ResultActions perform = this.mockMvc.perform(
                post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + requestAccessToken.getToken())
                        .cookie(requestCookie)
        );

        perform.andExpect(status().isUnauthorized())
                .andDo(document("auth/failure-refresh-when-invalid-refresh-token",
                        preprocessRequest(prettyPrint(), maskJwtFromHeader(EXPIRED_ACCESS_TOKEN), maskJwtFromCookie(INVALID_REFRESH_TOKEN)),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("access token")
                        )
                ));
    }
}

