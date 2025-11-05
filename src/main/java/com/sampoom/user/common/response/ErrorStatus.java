package com.sampoom.user.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    // 400 BAD_REQUEST
    TOO_SHORT_SECRET_KEY(HttpStatus.BAD_REQUEST, "서명용 비밀키의 길이가 짧습니다. 적어도 32바이트 이상으로 설정하세요.", 12402),
    BLANK_TOKEN(HttpStatus.BAD_REQUEST, "토큰 값은 공백이면 안됩니다.", 12400),
    NULL_TOKEN(HttpStatus.BAD_REQUEST, "토큰 값은 Null이면 안됩니다.", 12401),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다.", 11402),
    INVALID_WORKSPACE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 조직 타입입니다.", 11401),
    BLANK_TOKEN_ROLE(HttpStatus.BAD_REQUEST,"토큰 내 권한 정보가 공백입니다.",12404),
    NULL_TOKEN_ROLE(HttpStatus.BAD_REQUEST,"토큰 내 권한 정보가 NULL입니다.",12405),
    INVALID_REQUEST_ORGID(HttpStatus.BAD_REQUEST,"workspace 없이 organizationID로만 요청할 수 없습니다.",11403),

    // 401 UNAUTHORIZED
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", 12410),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 타입입니다. (토큰 타입 불일치)", 12412),
    NOT_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"토큰의 타입이 액세스 토큰이 아닙니다.",12413),
    NOT_SERVICE_TOKEN(HttpStatus.UNAUTHORIZED,"토큰의 타입이 서비스 토큰(내부 통신용 토큰)이 아닙니다.",12414),
    INVALID_TOKEN_ROLE(HttpStatus.UNAUTHORIZED,"유효하지 않은 토큰 내 권한 정보입니다. (토큰 권한 불일치)",12415),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.", 12411),

    // 403 FORBIDDEN
    ACCESS_DENIED(HttpStatus.FORBIDDEN,"접근 권한이 없어 접근이 거부되었습니다.",11430),

    // 404 NOT_FOUND
    NOT_FOUND_USER_BY_ID(HttpStatus.NOT_FOUND, "유저 고유 번호(userId)로 해당 유저를 찾을 수 없습니다.", 11440),
    NOT_FOUND_FACTORY_NAME(HttpStatus.NOT_FOUND, "지점명으로 공장 이름을 찾을 수 없습니다.", 10440),
    NOT_FOUND_WAREHOUSE_NAME(HttpStatus.NOT_FOUND, "지점명으로 창고 이름을 찾을 수 없습니다.", 10441),
    NOT_FOUND_AGENCY_NAME(HttpStatus.NOT_FOUND, "지점명으로 대리점 이름을 찾을 수 없습니다.", 10442),
    NOT_FOUND_USER_BY_WORKSPACE(HttpStatus.NOT_FOUND, "조직 내에서 유저를 찾을 수 없습니다.", 11441),
    NOT_FOUND_FACTORY_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 공장에서 해당 직원을 찾을 수 없습니다.",11442),
    NOT_FOUND_WAREHOUSE_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 공장에서 해당 직원을 찾을 수 없습니다.",11443),
    NOT_FOUND_AGENCY_EMPLOYEE(HttpStatus.NOT_FOUND,"전체 공장에서 해당 직원을 찾을 수 없습니다.",11444),

    // 409 CONFLICT
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 유저의 ID입니다.", 11491),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", 10500),
    INVALID_EVENT_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 형식이 유효하지 않습니다.", 10501),
    FAILED_CONNECTION_KAFKA(HttpStatus.INTERNAL_SERVER_ERROR,"Kafka 브로커 연결 또는 통신에 실패했습니다.",10503),
    EVENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Kafka 이벤트 처리 중 예외가 발생했습니다.",10504),

    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int code;


    public int getStatusCode() {
        return this.httpStatus.value();
    }
}