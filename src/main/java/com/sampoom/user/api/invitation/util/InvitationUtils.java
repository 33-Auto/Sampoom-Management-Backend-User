package com.sampoom.user.api.invitation.util;

import com.sampoom.user.common.exception.BadRequestException;
import com.sampoom.user.common.response.ErrorStatus;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class InvitationUtils {
    private static final SecureRandom RND = new SecureRandom();
    private InvitationUtils(){}

    public static String emailHash(String email, String salt) {
        // 입력 검증: 프로젝트 공통 예외 및 에러 상태 사용
        if (email == null)
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        if (salt == null)
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        if (email.isBlank())
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        if (salt.isBlank())
            throw new BadRequestException(ErrorStatus.INVALID_INPUT_VALUE);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String normalized = email.trim().toLowerCase();
            byte[] digest = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Hash error", e);
        }
    }

    public static String generateCode(int len) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789abcdefghijkmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);
        for (int i=0;i<len;i++) sb.append(chars.charAt(RND.nextInt(chars.length())));
        return sb.toString();
    }
}