package com.jakubwidlak.assignment.businesslogic;

import java.util.ArrayList;
import java.util.List;


public class ResponseObject {
    private String username;
    private List<String> userEmails;
    private List<String> userRepos;
    private List<String> languageStatistic;

    public ResponseObject() {
    }

    public ResponseObject(String username, ArrayList<String> userEmails, ArrayList<String> userRepos, List<String> languageStatistic) {
        this.username = username;
        this.userEmails = userEmails;
        this.userRepos = userRepos;
        this.languageStatistic = languageStatistic;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getUserEmails() {
        return userEmails;
    }

    public List<String> getUserRepos() {
        return userRepos;
    }

    public List<String> getLanguageStatistic() {
        return languageStatistic;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserEmails(List<String> userEmails) {
        this.userEmails = userEmails;
    }

    public void setUserRepos(List<String> userRepos) {
        this.userRepos = userRepos;
    }

    public void setLanguageStatistic(List<String> languageStatistic) {
        this.languageStatistic = languageStatistic;
    }
}
