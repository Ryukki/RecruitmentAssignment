package com.jakubwidlak.assignment.dataprovider;

import okhttp3.Request;

public class APIRequest{

    private String apiUrl = "https://api.github.com/";
    private Request request;

    public String getUrl() {
        return apiUrl;
    }

    public void setUrl(String url) {
        this.apiUrl = url;
    }

    /**
     * Builds request to the url being sum of apiUrl and given parameter. Specifies accepted content type as json.
     * @param requestPath directory on the server from apiUrl
     * @return Built request object
     */
    public Request buildRequest(String requestPath) {
        request = new Request.Builder()
                .url(apiUrl + requestPath)
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
