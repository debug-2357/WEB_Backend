package com.debug.api.controller.user;

import com.debug.api.dto.response.UserResponse;
import com.debug.api.service.UserService;
import com.debug.common.StatusEnum;
import com.debug.common.response.SuccessResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<SuccessResponseBody> getUser(@AuthenticationPrincipal UserDetails userDetails) {

        UserResponse userResponse = userService.getByUserId(userDetails.getUsername());
        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_USER_INFO, userResponse);
    }
}