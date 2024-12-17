package com.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * @author Zhangkunji
 * @date 2024/12/11
 * @Description
 */
public class BCryptUtil {
    public static String hashPassword(String password) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean verifyPassword(String inputPassword, String hashedPassword) {
        return BCrypt.checkpw(inputPassword, hashedPassword);
    }
}
