package com.vn.smart_space.utils;

import java.security.SecureRandom;
import java.util.Random;

public class OtpGenerator {
    private static final Random random = new SecureRandom();

    public static String generateOtp() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
