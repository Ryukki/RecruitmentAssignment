package com.jakubwidlak.assignment.dataprovider;

import okhttp3.Request;

public class APIRequest{

    private String url = "https://api.github.com/";
    private Request request;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Request buildRequest(String parameter) {
        request = new Request.Builder()
                .url(url + parameter)
                .get()
                .addHeader("content-type", "application/vnd.github.v3+json")
                .build();
        return request;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
