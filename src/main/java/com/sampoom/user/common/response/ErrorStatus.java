package com.sampoom.user.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 BAD_REQUEST
    SHORT_SECRET_KEY(HttpStatus.BAD_REQUEST, "서버에서 받은 서명용 비밀키의 길이가 짧습니다. 적어도 32바이트 이상으로 설정하세요", 10408),
    TOKEN_NULL_BLANK(HttpStatus.BAD_REQUEST, "토큰값은 Null이거나 공백이면 안됩니다.",10407),

    // 401 UNAUTHORIZED
    USER_PASSWORD_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 비밀번호입니다.",10402),
    TOKEN_TYPE_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 타입입니다.",10406),

    // 403 FORBIDDEN

    // 404 NOT_FOUND
    USER_BY_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 고유 번호(userId)로 해당 유저를 찾을 수 없습니다.", 10403),
    USER_BY_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일로 해당 유저를 찾을 수 없습니다.",10401),

    // 409 CONFLICT
    USER_ID_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 유저의 이메일입니다.", 10409),

    // 500 INTERNAL_SERVER_ERROR

    ;
    private final HttpStatus httpStatus;
    private final String message;
    private final int code;


    public int getStatusCode() {
        return this.httpStatus.value();
    }
}