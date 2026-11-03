package onl.jami.averUtils.discord.verification;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class VerificationCodes {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 8;

    private static final Cache<String, Long> pending = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public static String generateVerificationCode(Long discordId) {
        String code;
        do {
            StringBuilder sb = new StringBuilder(LENGTH);
            for (int i = 0; i < LENGTH; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (pending.getIfPresent(code) != null);
        pending.put(code, discordId);
        return code;
    }

    public static Long checkCode(String code) {
        if (code == null) return null;
        return pending.asMap().remove(code);
    }
}
