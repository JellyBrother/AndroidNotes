package com.jelly.app.main.head;

public class HttpHead extends Head {

    public void setCookie(String cookie) {
        put(HeadConstant.Key.COMMON_COOKIE, cookie);
    }

    public String getCookie() {
        return get(HeadConstant.Key.COMMON_COOKIE);
    }

    public void setToken(String token) {
        put(HeadConstant.Key.COMMON_TOKEN, token);
    }

    public String getToken() {
        return get(HeadConstant.Key.COMMON_TOKEN);
    }
}
