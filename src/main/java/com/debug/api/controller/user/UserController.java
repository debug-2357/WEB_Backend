package com.debug.api.controller.user;

import com.debug.api.entity.user.User;
import com.debug.api.service.UserService;
import com.debug.oauth.entity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Object> getUser(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUser(userDetails.getUsername());
        return new ResponseEntity<>(user.getUsername(), HttpStatus.OK);
    }
}