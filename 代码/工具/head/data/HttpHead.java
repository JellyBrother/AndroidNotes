package com.jelly.app.main.head.data;

import com.jelly.app.main.head.HeadConstant;

public class HttpHead extends Head {

    public void setCookie(String cookie) {
        put(HeadConstant.Key.COMMON_COOKIE, cookie);
    }

    public String getCookie() {
        return get(HeadConstant.Key.COMMON_COOKIE);
    }

    public void setCookieUrl(String cookieUrl) {
        put(HeadConstant.Key.COMMON_COOKIE_URL, cookieUrl);
    }

    public void setToken(String token) {
        put(HeadConstant.Key.COMMON_TOKEN, token);
    }

    public String getToken() {
        return get(HeadConstant.Key.COMMON_TOKEN);
    }
}
