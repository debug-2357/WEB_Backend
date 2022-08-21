package com.debug.docs.user;

import com.debug.api.controller.user.UserController;
import com.debug.api.dto.request.RegisterRequest;
import com.debug.api.dto.response.UserResponse;
import com.debug.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("development")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String ACCESS_TOKEN = "{example_access_token}";

    @Test
    @DisplayName("유저 정보 요청 성공")
    @WithMockUser
    public void test_success_get_user() throws Exception {
        // given
        UserResponse userResponse = UserResponse.builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .profileImageUrl("url")
                .role("ROLE_GUEST")
                .build();

        // when
        when(userService.getByUserId(anyString()))
                .thenReturn(userResponse);

        // when, then
        this.mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("user/success-get-user",
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT access token")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.userId").description("이름"),
                                fieldWithPath("data.username").description("유저 별명"),
                                fieldWithPath("data.email").description("이메일"),
                                fieldWithPath("data.profileImageUrl").description("프로필 사진 url"),
                                fieldWithPath("data.role").description("유저 role")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 성공")
    public void test_success_sign_up() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test1")
                .build();

        // when, then
        this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andDo(document("user/success-sign-up",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("userId").description("이름"),
                                fieldWithPath("username").description("유저 별명"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password1").description("패스워드"),
                                fieldWithPath("password2").description("패스워드 확인")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 불일치로 인한 회원가입 실패")
    public void test_failure_sign_up_when_invalid_password() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test2")
                .build();

        // when, then
        this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andDo(document("user/failure-sign-up-when-invalid-password", preprocessResponse(prettyPrint())));
    }

    @Test
    @DisplayName("중복된 userid로 인해 회원가입 실패")
    public void test_failure_sign_up_when_user_id_already_in_use() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test1")
                .build();

        // when
        when(userService.existsByUserId(anyString()))
                .thenReturn(true);

        // when, then
        this.mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andDo(document("user/failure-sign-up-when-user-id-already-in-use", preprocessResponse(prettyPrint())));
    }

    @Test
    @DisplayName("unconfirmed 변경 성공")
    @WithMockUser
    public void test_success_change_unconfirmed() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test1")
                .build();

        // when, then
        this.mockMvc.perform(
                patch("/api/users")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andDo(document("user/success-change-unconfirmed"));
    }

    @Test
    @DisplayName("비밀번호 불일치로 인한 unconfirmed 변경 실패")
    public void test_failure_change_unconfirmed_invalid_password() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test2")
                .build();

        // when, then
        this.mockMvc.perform(
                patch("/api/users")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andDo(document("user/failure-change-unconfirmed-when-invalid-password", preprocessResponse(prettyPrint())));

    }

    @Test
    @DisplayName("중복된 userid로 인해 unconfirmed 변경 실패")
    public void test_failure_change_unconfirmed_when_user_id_already_in_use() throws Exception {
        // given
        RegisterRequest registerRequest = RegisterRequest
                .builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .password1("test1")
                .password2("test1")
                .build();

        // when
        when(userService.existsByUserId(anyString()))
                .thenReturn(true);

        // when, then
        this.mockMvc.perform(
                patch("/api/users")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andDo(document("user/failure-change-unconfirmed-when-user-id-already-in-use", preprocessResponse(prettyPrint())));
    }
}
