package com.sampoom.backend.user.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 BAD_REQUEST

    // 401 UNAUTHORIZED
    USER_PASSWORD_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 비밀번호입니다.",10402),

    // 403 FORBIDDEN

    // 404 NOT_FOUND
    USER_BY_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 고유 번호(userId)로 해당 유저를 찾을 수 없습니다.", 10403),
    USER_BY_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일로 해당 유저를 찾을 수 없습니다.",10401),

    // 409 CONFLICT
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 유저의 이메일입니다.", 10400),

    // 500 INTERNAL_SERVER_ERROR

    ;
    private final HttpStatus httpStatus;
    private final String message;
    private final int code;


    public int getStatusCode() {
        return this.httpStatus.value();
    }
}