package com.debug.api.dto.request;

import lombok.Getter;

@Getter
public class RegisterRequest {
    private String userId;
    private String username;
    private String password1;
    private String password2;
    private String email;
}
