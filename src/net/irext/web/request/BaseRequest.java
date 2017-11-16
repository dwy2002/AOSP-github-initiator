package net.irext.web.request;

import com.google.gson.Gson;

/**
 * Filename:       BaseRequest.java
 * Revised:        Date: 2017-04-07
 * Revision:       Revision: 1.0
 * <p>
 * Description:    authentication factors included
 * <p>
 * Revision log:
 * 2017-11-13: created by strawmanbobi
 */
public class BaseRequest {

    private String token;

    public BaseRequest(String token) {
        this.token = token;
    }

    BaseRequest() {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJson() {
        return new Gson().toJson(this, this.getClass());
    }
}
