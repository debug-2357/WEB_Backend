package com.debug.docs.security;

import com.debug.api.controller.user.UserController;
import com.debug.api.repository.user.UserRefreshTokenRepository;
import com.debug.api.service.UserRefreshTokenService;
import com.debug.api.service.UserService;
import com.debug.config.properties.AppProperties;
import com.debug.config.properties.CorsProperties;
import com.debug.config.security.SecurityConfig;
import com.debug.oauth.entity.RoleType;
import com.debug.oauth.handler.TokenAccessDeniedHandler;
import com.debug.oauth.service.CustomOAuth2UserService;
import com.debug.oauth.service.CustomUserDetailsService;
import com.debug.oauth.token.AuthToken;
import com.debug.oauth.token.AuthTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Date;

import static com.debug.docs.common.CustomPreprocessors.maskJwtFromHeader;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("development")
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs
@Import({
        SecurityConfig.class,
        AppProperties.class,
        CorsProperties.class,
        AuthTokenProvider.class,
        TokenAccessDeniedHandler.class
})
@MockBeans({
        @MockBean(CustomUserDetailsService.class),
        @MockBean(CustomOAuth2UserService.class),
        @MockBean(UserRefreshTokenRepository.class),
        @MockBean(UserRefreshTokenService.class)
})
public class AccessExceptionHandleTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @MockBean
    private UserService userService;

    private final static String NORMAL_ACCESS_TOKEN = "{정상적인 토큰}";
    private final static String INVALID_ACCESS_TOKEN = "{위조 또는 잘못된 토큰}";
    private final static String EXPIRED_ACCESS_TOKEN = "{만료된 토큰}";

    @Test
    @DisplayName("access token이 포함이 안된 상태로 요청보낼 경우")
    public void test_unauthorized_when_access_token_is_null() throws Exception {
        // when
        ResultActions perform = this.mockMvc.perform(get("/api/users"));

        // then
        perform.andExpect(status().isUnauthorized())
                .andDo(document("access-exception-handle/unauthorized-when-access-token-is-null",
                        preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @DisplayName("access token이 만료 됬을 경우")
    public void test_unauthorized_when_access_token_is_expired() throws Exception {
        // given
        AuthToken authToken = authTokenProvider.createAuthToken("test", RoleType.GUEST.getAuthority(), new Date(0));

        // when
        ResultActions perform = this.mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer " + authToken.getToken())
        );

        // then
        perform.andExpect(status().isUnauthorized())
                .andDo(document("access-exception-handle/unauthorized-when-access-token-is-expired",
                                preprocessRequest(prettyPrint(), maskJwtFromHeader(EXPIRED_ACCESS_TOKEN)),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @DisplayName("access token이 위조 됬을 경우")
    public void test_unauthorized_when_access_token_is_invalid() throws Exception {
        // given
        Date now = new Date();
        AuthToken authToken = authTokenProvider.createAuthToken(
                "test", RoleType.GUEST.getAuthority(), new Date(now.getTime() + 100000)
        );

        // when
        ResultActions perform = this.mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer " + authToken.getToken() + "test")
        );

        // then
        perform.andExpect(status().isUnauthorized())
                .andDo(document("access-exception-handle/unauthorized-when-access-token-is-invalid",
                                preprocessRequest(prettyPrint(), maskJwtFromHeader(INVALID_ACCESS_TOKEN)),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @DisplayName("필터링 된 유저의 role이 ROLE_GUEST이며 접근 불가능 한 url로 요청한 경우")
    public void test_forbidden_when_role_guest_accesses_disallowed_url() throws Exception {
        // given
        Date now = new Date();
        AuthToken authToken = authTokenProvider.createAuthToken(
                "test", RoleType.GUEST.getAuthority(), new Date(now.getTime() + 100000)
        );

        // when
        ResultActions perform = this.mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer " + authToken.getToken())
        );

        // then
        perform.andExpect(status().isForbidden())
                .andDo(document("access-exception-handle/forbidden-when-role-guest-accesses-disallowed-url",
                                preprocessRequest(prettyPrint(), maskJwtFromHeader(NORMAL_ACCESS_TOKEN)),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    @DisplayName("필터링 된 유저의 role이 ROLE_GUEST가 아닌 인증자이며 접근 불가능 한 url로 요청한 경우")
    public void test_forbidden_when_a_non_role_guest_authenticator_accesses_a_disallowed_url() throws Exception {
        // given
        Date now = new Date();
        AuthToken authToken = authTokenProvider.createAuthToken(
                "test", RoleType.GUEST.getAuthority(), new Date(now.getTime() + 100000)
        );

        // when
        ResultActions perform = this.mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer " + authToken.getToken())
        );

        // then
        perform.andExpect(status().isForbidden())
                .andDo(document("access-exception-handle/forbidden-when-a-non-role-guest-authenticator-accesses-a-disallowed-url",
                                preprocessRequest(prettyPrint(), maskJwtFromHeader(NORMAL_ACCESS_TOKEN)),
                                preprocessResponse(prettyPrint())
                        )
                );
    }
}
