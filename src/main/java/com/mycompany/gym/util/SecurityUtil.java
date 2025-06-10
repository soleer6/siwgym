// src/main/java/com/mycompany/gym/util/SecurityUtil.java
package com.mycompany.gym.util;

import javax.servlet.http.HttpServletRequest;

public class SecurityUtil {

    public static boolean isLoggedIn(HttpServletRequest request) {
        Object userIdObj = request.getSession().getAttribute("userId");
        return userIdObj != null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        Object roleObj = request.getSession().getAttribute("role");
        if (roleObj instanceof String) {
            return ((String) roleObj).equals("ADMIN");
        }
        return false;
    }
}
