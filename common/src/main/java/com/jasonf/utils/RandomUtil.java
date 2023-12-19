package com.jasonf.utils;

import java.util.Random;

public class RandomUtil {
    private static final String DICT = "abcdefghijklmnopqrstuvwxyz0123456789";

    private static final int LEN = 16;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static String getRandomString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LEN; i++) {
            int index = RANDOM.nextInt(DICT.length());
            sb.append(DICT.charAt(index));
        }
        return sb.toString();
    }
}
