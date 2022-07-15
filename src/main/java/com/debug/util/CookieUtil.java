package com.debug.util;


import org.springframework.util.SerializationUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtil {

    /**
     * name에 해당하는 cookie를 반환
     * @param request 클라이언트 request
     * @param name cookie name
     * @return cookie or empty Optional
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            return Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .findFirst();
        }

        return Optional.empty();
    }

    /**
     * 쿠키를 response에 추가
     * @param response 클라이언트 response
     * @param name cookie name
     * @param value cookie value
     * @param maxAge cookie max age
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제
     * @param request 클라이언트 request
     * @param response 클라이언트 response
     * @param name cookie name
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .forEach(cookie -> {
                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    });
        }
    }

    /**
     * 직렬화
     * @param obj object
     * @return Base64
     */
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    /**
     * 역 직렬화
     * @param cookie cookie
     * @param tClass tClass
     * @return tClass
     * @param <T> generic class
     */
    public static <T> T deserialize(Cookie cookie, Class<T> tClass) {
        return tClass.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }
}
