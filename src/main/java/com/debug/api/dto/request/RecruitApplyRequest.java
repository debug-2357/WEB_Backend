package com.debug.api.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RecruitApplyRequest {

    private Map<String, String> content;
}
