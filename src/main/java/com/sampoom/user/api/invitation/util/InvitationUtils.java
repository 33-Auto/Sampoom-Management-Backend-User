package com.sampoom.user.api.invitation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class InvitationUtils {
    private static final SecureRandom RND = new SecureRandom();
    private InvitationUtils(){}

    public static String emailHash(String email, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((email.trim().toLowerCase() + "|" + salt).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest());
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