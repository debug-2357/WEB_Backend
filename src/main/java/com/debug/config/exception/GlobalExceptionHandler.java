package com.debug.config.exception;

import com.debug.api.exception.*;
import com.debug.common.response.FailureResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.debug")
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAccessTokenException.class)
    public ResponseEntity<FailureResponseBody> handleInvalidAccessTokenException(InvalidAccessTokenException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(ExpiredAccessTokenException.class)
    public ResponseEntity<FailureResponseBody> handleExpiredAccessTokenException(ExpiredAccessTokenException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<FailureResponseBody> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<FailureResponseBody> handleLoginFailedException(LoginFailedException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<FailureResponseBody> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(UnexpiredAccessTokenException.class)
    public ResponseEntity<FailureResponseBody> handleUnexpiredAccessTokenException(UnexpiredAccessTokenException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<FailureResponseBody> handleUserNotFoundException(UserNotFoundException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<FailureResponseBody> handleInvalidPasswordException(InvalidPasswordException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(UserIdAlreadyInUseException.class)
    public ResponseEntity<FailureResponseBody> handleUserIdAlreadyInUseException(UserIdAlreadyInUseException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(NonReceptionPeriodException.class)
    public ResponseEntity<FailureResponseBody> handleNonReceptionPeriodException(NonReceptionPeriodException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(RecruitPeriodNotFoundException.class)
    public ResponseEntity<FailureResponseBody> handleRecruitPeriodNotFoundException(RecruitPeriodNotFoundException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(AlreadyAppliedByUserException.class)
    public ResponseEntity<FailureResponseBody> handleAlreadyAppliedByUserException(AlreadyAppliedByUserException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }

    @ExceptionHandler(RecruitApplyNotFoundException.class)
    public ResponseEntity<FailureResponseBody> handleRecruitApplyNotFoundException(RecruitApplyNotFoundException e) {
        return FailureResponseBody.toResponseEntity(e.getStatusEnum());
    }
}
