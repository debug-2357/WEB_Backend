package com.debug.api.controller.user;

import com.debug.api.dto.request.RegisterRequest;
import com.debug.api.dto.response.UserResponse;
import com.debug.common.exception.InvalidPasswordException;
import com.debug.common.exception.UserIdAlreadyInUseException;
import com.debug.api.service.user.UserService;
import com.debug.common.StatusEnum;
import com.debug.common.response.SuccessResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<SuccessResponseBody> getMyUser(@AuthenticationPrincipal UserDetails userDetails) {

        UserResponse userResponse = userService.getByUserId(userDetails.getUsername());
        return SuccessResponseBody.toResponseEntity(StatusEnum.GET_USER_INFO, userResponse);
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<SuccessResponseBody> findByUserId(@PathVariable String userId) {

        boolean isExistUser = userService.existsByUserId(userId);
        StatusEnum status = (isExistUser) ? StatusEnum.USER_EXISTS : StatusEnum.USER_DOSE_NOT_EXIST;
        return SuccessResponseBody.toResponseEntity(status, null);
    }

    @PostMapping
    public ResponseEntity<SuccessResponseBody> createUser(@RequestBody RegisterRequest registerRequest) {
        if (!comparePassword(registerRequest)) {
            throw new InvalidPasswordException();
        }

        if (userService.existsByUserId(registerRequest.getUserId())) {
            throw new UserIdAlreadyInUseException();
        }

        userService.createUser(registerRequest);
        return SuccessResponseBody.toResponseEntity(StatusEnum.CREATE_USER, null);
    }

    // TODO : 2022.08.25 Oauth2 유저는 email과 username을 바꿀 필요없음 dto request를 새로 생성합시다.
    @PatchMapping
    public ResponseEntity<SuccessResponseBody> changeUnconfirmed(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody RegisterRequest registerRequest) {
        if (!comparePassword(registerRequest)) {
            throw new InvalidPasswordException();
        }

        if (userService.existsByUserId(registerRequest.getUserId())) {
            throw new UserIdAlreadyInUseException();
        }

        userService.changeUnconfirmed(userDetails.getUsername(), registerRequest);
        return SuccessResponseBody.toResponseEntity(StatusEnum.SUCCESS_SIGN_UP, null);
    }

    private boolean comparePassword(RegisterRequest registerRequest) {
        return registerRequest.getPassword1().equals(registerRequest.getPassword2());
    }
}