package com.debug.api.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthRequest {
    private String id;
    private String password;
}
