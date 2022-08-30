package com.debug.docs.recruit;

import com.debug.api.controller.recruit.RecruitController;
import com.debug.api.dto.response.RecruitApplyResponse;
import com.debug.api.dto.response.RecruitPeriodResponse;
import com.debug.api.dto.response.UserResponse;
import com.debug.api.service.recruit.RecruitApplyService;
import com.debug.api.service.recruit.RecruitPeriodService;
import com.debug.common.exception.*;
import com.debug.config.exception.GlobalExceptionHandler;
import com.debug.oauth.entity.RoleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("development")
@WebMvcTest(RecruitController.class)
@Import({GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
public class RecruitControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private RecruitPeriodService recruitPeriodService;

    @MockBean
    private RecruitApplyService recruitApplyService;

    private List<RecruitPeriodResponse> recruitPeriodResponseList;
    private RecruitPeriodResponse recentRecruitPeriodResponse;
    private RecruitPeriodResponse canNotApplyByRecruitPeriodResponse;
    private UserResponse userResponse;
    private List<RecruitApplyResponse> recruitApplyResponseList;
    private Map<String, String> recruitApplyRequest;
    private final static String ACCESS_TOKEN_WITHOUT_GUEST = "{role이 ROLE_GUEST가 아닌 유저}";
    private final static String ACCESS_TOKEN_WITH_UNCONFIRMED = "{role이 ROLE_UNCONFIRMED인 access token}";

    @BeforeEach
    public void setUp() {
        this.objectMapper = Jackson2ObjectMapperBuilder.json().build();
        this.recruitPeriodResponseList = setTestRecruitPeriodResponses();
        this.recentRecruitPeriodResponse = recruitPeriodResponseList.get(0);
        this.canNotApplyByRecruitPeriodResponse = recruitPeriodResponseList.get(2);
        this.userResponse = setTestUserResponse();
        this.recruitApplyResponseList = setTestRecruitApplyResponse(userResponse, recentRecruitPeriodResponse);
        this.recruitApplyRequest = recruitApplyResponseList.get(0).getContent();
    }

    private List<RecruitPeriodResponse> setTestRecruitPeriodResponses() {
        List<RecruitPeriodResponse> recruitPeriodResponses = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime[][] times = {
                {LocalDateTime.of(2021, 3, 1, 0, 0), LocalDateTime.of(2021, 3, 1, 23, 59)},
                {LocalDateTime.of(2022, 3, 15, 0, 0), LocalDateTime.of(2022, 3, 15, 23, 59)},
                {LocalDateTime.now().minusWeeks(1), LocalDateTime.now().plusWeeks(1)}
        };

        for (int i = 3; i >= 1; i--) {
            List<String> questions = new ArrayList<>();

            for (int j = 1; j <= 3; j++) {
                questions.add(i + "기 질문" + j);
            }

            RecruitPeriodResponse recruitPeriodResponse = RecruitPeriodResponse.builder()
                    .id((long) i)
                    .yearOf(i + "기")
                    .questions(questions)
                    .startDate(times[i - 1][0])
                    .endDate(times[i - 1][1])
                    .build();

            recruitPeriodResponses.add(recruitPeriodResponse);
        }

        return recruitPeriodResponses;
    }

    private UserResponse setTestUserResponse() {

        return UserResponse.builder()
                .userId("test")
                .username("test_name")
                .email("test@gmail.com")
                .profileImageUrl("url")
                .role(RoleType.UNCONFIRMED.getAuthority())
                .build();
    }

    private List<RecruitApplyResponse> setTestRecruitApplyResponse(UserResponse userResponse, RecruitPeriodResponse recruitPeriodResponse) {
        Map<String, String> content = new LinkedHashMap<>();
        int n = 1;
        for (String question : recruitPeriodResponse.getQuestions()) {
            content.put(question, "답변" + n++);
        }

        LocalDateTime now = LocalDateTime.now();

        RecruitApplyResponse recruitApplyResponse = RecruitApplyResponse.builder()
                .id(1L)
                .user(userResponse)
                .recruitPeriod(recruitPeriodResponse)
                .content(content)
                .isPass("검토중")
                .createdDate(now)
                .modifiedDate(now)
                .build();

        return List.of(recruitApplyResponse);
    }

    @Test
    @DisplayName("모든 지원 기간 정보 요청 성공")
    public void success_find_all_recruit_periods() throws Exception {

        when(recruitPeriodService.findAll())
                .thenReturn(recruitPeriodResponseList);

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods")
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/period/success-find-all-recruit-periods",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data[].id").description("Recruit period PK"),
                                fieldWithPath("data[].yearOf").description("기수 이름"),
                                fieldWithPath("data[].startDate").description("지원 시작 시간"),
                                fieldWithPath("data[].endDate").description("지원 마감 시간"),
                                fieldWithPath("data[].questions").description("지원 질문들")
                        )
                ));
    }

    @Test
    @DisplayName("모든 지원 기간을 정보 요청 데이터 없음")
    public void no_recruit_periods_content() throws Exception {
        when(recruitPeriodService.findAll())
                .thenReturn(List.of());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods")
        );

        perform.andExpect(status().isNoContent())
                .andDo(document("recruit/period/no-recruit-periods-content",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data").description("데이터").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("지원 가능한 최신 지원 기간 정보 요청 성공")
    public void success_get_recent_recruit_period() throws Exception {
        when(recruitPeriodService.getRecentRecruitPeriod())
                .thenReturn(recentRecruitPeriodResponse);

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/recent")
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/period/success-get-recent-recruit-period",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.id").description("Recruit period PK"),
                                fieldWithPath("data.yearOf").description("기수 이름"),
                                fieldWithPath("data.startDate").description("지원 시작 시간"),
                                fieldWithPath("data.endDate").description("지원 마감 시간"),
                                fieldWithPath("data.questions").description("지원 질문들")
                        )
                ));
    }

    @Test
    @DisplayName("지원 가능한 최신 지원 기간 정보 요청 실패 (db에 지원기간이 없음)")
    public void failure_get_recent_recruit_period_when_recruit_period_not_found() throws Exception {
        when(recruitPeriodService.getRecentRecruitPeriod())
                .thenThrow(new RecruitPeriodNotFoundException());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/recent")
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/period/failure-get-recent-recruit-period-when-recruit-period-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("지원 가능한 최신 지원 기간 정보 요청 실패 (요청한 시간이 지원기간이 아님.)")
    public void failure_get_recent_recruit_period_when_request_can_not_apply() throws Exception {
        when(recruitPeriodService.getRecentRecruitPeriod())
                .thenThrow(new NonReceptionPeriodException());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/recent")
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("recruit/period/failure-get-recent-recruit-period-when-request-can-not-apply",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("특정 지원 기간 정보 요청 성공")
    public void success_find_by_recruit_period_id() throws Exception {
        when(recruitPeriodService.findByRecruitPeriodId(anyLong()))
                .thenReturn(recentRecruitPeriodResponse);

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/{recruitPeriodId}", 3)
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/period/success-find-by-recruit-period-id",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("recruitPeriodId").description("Recruit Period PK")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data.id").description("Recruit period PK"),
                                fieldWithPath("data.yearOf").description("기수 이름"),
                                fieldWithPath("data.startDate").description("지원 시작 시간"),
                                fieldWithPath("data.endDate").description("지원 마감 시간"),
                                fieldWithPath("data.questions").description("지원 질문들")
                        )
                ));
    }

    @Test
    @DisplayName("특정 지원 기간 정보 요청 실패 (해당 id로 존재하는 데이터 없음)")
    public void failure_find_by_recruit_period_id() throws Exception {
        when(recruitPeriodService.findByRecruitPeriodId(anyLong()))
                .thenThrow(new RecruitPeriodNotFoundException());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/{recruitPeriodId}", 4)
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/period/failure-find-by-recruit-period-id",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("recruitPeriodId").description("Recruit Period PK")
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored()
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("현재 로그인 중인 유저의 모든 지원서 정보 요청 성공")
    public void success_find_all_my_applies() throws Exception {
        when(recruitApplyService.getAppliesByUserId(anyString()))
                .thenReturn(recruitApplyResponseList);

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/applies")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITHOUT_GUEST)
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/apply/success-find-all-my-applies",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITHOUT_GUEST)
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data[].id").description("지원서 PK"),
                                fieldWithPath("data[].user").description("지원서 유저 정보"),
                                fieldWithPath("data[].user.userId").ignored(),
                                fieldWithPath("data[].user.username").ignored(),
                                fieldWithPath("data[].user.email").ignored(),
                                fieldWithPath("data[].user.profileImageUrl").ignored(),
                                fieldWithPath("data[].user.role").ignored(),
                                fieldWithPath("data[].recruitPeriod.id").ignored(),
                                fieldWithPath("data[].recruitPeriod.yearOf").ignored(),
                                fieldWithPath("data[].recruitPeriod.startDate").ignored(),
                                fieldWithPath("data[].recruitPeriod.endDate").ignored(),
                                fieldWithPath("data[].recruitPeriod.questions").ignored(),
                                fieldWithPath("data[].recruitPeriod").description("지원 기간 정보"),
                                fieldWithPath("data[].content").description("질문에 대한 답변들"),
                                fieldWithPath("data[].content.3기 질문1").ignored(),
                                fieldWithPath("data[].content.3기 질문2").ignored(),
                                fieldWithPath("data[].content.3기 질문3").ignored(),
                                fieldWithPath("data[].isPass").description("합격 여부(검토중, 합격, 불합격)"),
                                fieldWithPath("data[].createdDate").description("생성 시간"),
                                fieldWithPath("data[].modifiedDate").description("수정 시간")
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("현재 로그인 중인 유저의 모든 지원서 정보 요청했지만 데이터가 없는 경우")
    public void no_my_applies_content() throws Exception {
        when(recruitApplyService.getAppliesByUserId(anyString()))
                .thenReturn(List.of());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/applies")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITHOUT_GUEST)
        );

        perform.andExpect(status().isNoContent())
                .andDo(document("recruit/apply/no-my-applies-content",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITHOUT_GUEST)
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data").ignored()
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("현재 로그인 중인 유저의 모든 지원서 정보 요청했지만 데이터가 없는 경우")
    public void failure_find_all_my_applies_when_not_found_user() throws Exception {
        when(recruitApplyService.getAppliesByUserId(anyString()))
                .thenThrow(new UserNotFoundException());

        ResultActions perform = this.mockMvc.perform(
                get("/api/recruit/periods/applies")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITHOUT_GUEST)
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/apply/failure-find-all-my-applies-when-not-found-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITHOUT_GUEST)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("지원 가능한 기수에 지원서 작성 성공")
    public void success_create_apply() throws Exception {
        when(recruitApplyService.save(anyLong(), anyString(), any()))
                .thenReturn(1L);

        ResultActions perform = this.mockMvc.perform(
                post("/api/recruit/periods/{periodId}/applies", 3)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isCreated())
                .andDo(document("recruit/apply/success-create-apply",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data").ignored(),
                                fieldWithPath("data.targetPath").description("접근 할 수 있는 지원서 uri")
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("periodId에 존재하는 지원기간 정보가 없어서 지원서 작성 실패")
    public void failure_create_apply_when_recruit_period_not_found() throws Exception {
        when(recruitApplyService.save(anyLong(), anyString(), any()))
                .thenThrow(new RecruitPeriodNotFoundException());

        ResultActions perform = this.mockMvc.perform(
                post("/api/recruit/periods/{periodId}/applies", 3)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/apply/failure-create-apply-when-recruit-period-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("데이터는 불러왔지만 지원기간이 아니여서 지원서 작성 실패")
    public void failure_create_apply_when_the_application_period_is_not() throws Exception {
        when(recruitApplyService.save(anyLong(), anyString(), any()))
                .thenThrow(new NonReceptionPeriodException());

        ResultActions perform = this.mockMvc.perform(
                post("/api/recruit/periods/{periodId}/applies", 3)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("recruit/apply/failure-create-apply-when-the-application-period-is-not",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("이미 로그인된 유저가 해당 지원기간에 지원서가 있을 때 지원서 작성 실패")
    public void failure_create_apply_when_already_applied_by_user() throws Exception {
        when(recruitApplyService.save(anyLong(), anyString(), any()))
                .thenThrow(new AlreadyAppliedByUserException());

        ResultActions perform = this.mockMvc.perform(
                post("/api/recruit/periods/{periodId}/applies", 3)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("recruit/apply/failure-create-apply-when-already-applied-by-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("지원 가능한 기수에 지원서 수정 성공")
    public void success_update_apply() throws Exception {
        when(recruitApplyService.update(anyLong(), anyLong(), anyString(), any()))
                .thenReturn(1L);

        ResultActions perform = this.mockMvc.perform(
                patch("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/apply/success-update-apply",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("요청 시간").ignored(),
                                fieldWithPath("status").description("http status").ignored(),
                                fieldWithPath("statusDetail").description("http status 설명").ignored(),
                                fieldWithPath("code").description("서버 내 코드").ignored(),
                                fieldWithPath("message").description("코드 설명").ignored(),
                                fieldWithPath("data").ignored(),
                                fieldWithPath("data.targetPath").description("접근 할 수 있는 지원서 uri")
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("해당 지원서가 존재하지 않아 지원서 수정 실패")
    public void failure_update_apply_when_recruit_apply_not_found() throws Exception {
        when(recruitApplyService.update(anyLong(), anyLong(), anyString(), any()))
                .thenThrow(new RecruitApplyNotFoundException());

        ResultActions perform = this.mockMvc.perform(
                patch("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/apply/failure-update-apply-when-recruit-apply-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }



    @Test
    @WithMockUser("test")
    @DisplayName("로그인 된 유저와 지원서를 작성한 유저가 다를때 지원서 수정 실패")
    public void failure_update_apply_when_no_apply_permission() throws Exception {
        when(recruitApplyService.update(anyLong(), anyLong(), anyString(), any()))
                .thenThrow(new NoRecruitApplyPermissionException());

        ResultActions perform = this.mockMvc.perform(
                patch("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isForbidden())
                .andDo(document("recruit/apply/failure-update-apply-when-no_apply_permission",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("데이터는 불러왔지만 지원기간이 아니여서 지원서 수정 실패")
    public void failure_update_apply_when_the_application_period_is_not() throws Exception {
        when(recruitApplyService.update(anyLong(), anyLong(), anyString(), any()))
                .thenThrow(new NonReceptionPeriodException());

        ResultActions perform = this.mockMvc.perform(
                patch("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recruitApplyRequest))
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("recruit/apply/failure-update-apply-when-the-application-period-is-not",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("지원서 삭제 성공")
    public void success_delete_recruit_apply() throws Exception {
        ResultActions perform = this.mockMvc.perform(
                delete("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
        );

        perform.andExpect(status().isOk())
                .andDo(document("recruit/apply/success-delete-recruit-apply",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("지원서가 존재 하지 않을 때 삭제 실패")
    public void failure_delete_recruit_apply_when_recruit_apply_not_found() throws Exception {
        doThrow(new RecruitApplyNotFoundException())
                .when(recruitApplyService).delete(anyLong(), anyLong(), anyString());

        ResultActions perform = this.mockMvc.perform(
                delete("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
        );

        perform.andExpect(status().isNotFound())
                .andDo(document("recruit/apply/failure-delete-recruit-apply-when-recruit-apply-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("현재 로그인 된 유저와 지원서 유저가 다를 때 삭제 실패")
    public void failure_delete_recruit_apply_when_no_recruit_apply_permission() throws Exception {
        doThrow(new NoRecruitApplyPermissionException())
                .when(recruitApplyService).delete(anyLong(), anyLong(), anyString());

        ResultActions perform = this.mockMvc.perform(
                delete("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
        );

        perform.andExpect(status().isForbidden())
                .andDo(document("recruit/apply/failure-delete-recruit-apply-when-no-recruit-apply-permission",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }

    @Test
    @WithMockUser("test")
    @DisplayName("지원 기간이 아닐 때 삭제 실패")
    public void failure_delete_recruit_apply_when_non_reception_period() throws Exception {
        doThrow(new NonReceptionPeriodException())
                .when(recruitApplyService).delete(anyLong(), anyLong(), anyString());

        ResultActions perform = this.mockMvc.perform(
                delete("/api/recruit/periods/{periodId}/applies/{applyId}", 3, 1)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN_WITH_UNCONFIRMED)
        );

        perform.andExpect(status().isBadRequest())
                .andDo(document("recruit/apply/failure-delete-recruit-apply-when-non-reception-period",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("periodId").description("지원 가능한 Recruit Period PK"),
                                parameterWithName("applyId").description("수정 하고 싶은 지원서")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description(ACCESS_TOKEN_WITH_UNCONFIRMED)
                        )
                ));
    }
}
