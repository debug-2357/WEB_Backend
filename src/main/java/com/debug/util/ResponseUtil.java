package com.debug.util;

import com.debug.common.StatusEnum;
import com.debug.common.response.FailureResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {

    public static void setResponse(HttpServletResponse response, StatusEnum statusEnum) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(statusEnum.getHttpStatus().value());

        FailureResponseBody body = FailureResponseBody.builder()
                .status(statusEnum.getHttpStatus().value())
                .statusDetail(statusEnum.getHttpStatus().name())
                .code(statusEnum.name())
                .message(statusEnum.getDetail())
                .build();

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        String jsonBody = objectMapper.writeValueAsString(body);

        response.getWriter().print(jsonBody);
    }
}
