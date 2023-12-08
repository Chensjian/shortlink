package com.chen.shortlink.admin.util;

import java.util.Random;

/**
 * 随机码工具类
 */
public class RandomCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 6;

    /**
     * 返回一个长度为6，英文加数字的随机码
     * @return
     */
    public static String generateRandomString() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
