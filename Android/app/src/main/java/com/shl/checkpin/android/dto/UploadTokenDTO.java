package com.shl.checkpin.android.dto;

/**
 * Created by vfedin on 28.11.2015.
 */
public class UploadTokenDTO {
    private long expires;
    String token;

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
