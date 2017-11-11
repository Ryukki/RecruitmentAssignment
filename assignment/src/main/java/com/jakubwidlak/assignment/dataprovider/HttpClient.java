package com.jakubwidlak.assignment.dataprovider;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class HttpClient extends OkHttpClient {
    private String user;
    private static final String errorMessage = "Ooops, something went wrong :( ";
    private static final String responseIssue = "Please check GitHub API status.";
    private static final String exceptionMessage = "Contact app provider.";
    private static final String stringTotalBytes= "totalBytes";

    public boolean setUser(String user) {
        this.user = user;
        Response responseUser = null;
        try {
            APIRequest apiRequest = new APIRequest("users/" + user);
            responseUser = this.newCall(apiRequest.getRequest()).execute();
            if (!responseUser.isSuccessful()) {
                this.user = "Sorry user might not exist. Please check GitHub API status.";
                return false;
            }
        } catch (IOException e) {
            this.user = "Sorry user might not exist. Please check GitHub API status.";
            e.printStackTrace();
            return false;
        } finally {
            responseUser.close();
        }
        return true;
    }

    public String getUser() {
        return user;
    }

    public List<String> getUserEmails(){
        List<String> emailsList = new ArrayList<>();
        Response responseUserEmails = null;
        try {
            APIRequest apiRequest = new APIRequest("users/" + user + "/events");
            responseUserEmails = this.newCall(apiRequest.getRequest()).execute();
            if(!responseUserEmails.isSuccessful()){
                emailsList.add(errorMessage);
                emailsList.add(responseIssue);
                return emailsList;
            }

            String stringUserEmails = responseUserEmails.body().string();
            JSONArray jsonUserEmails = new JSONArray(stringUserEmails);
            for (int i =0; i< jsonUserEmails.length(); i++){
                JSONObject jsonObject = jsonUserEmails.getJSONObject(i);
                JSONObject payload = jsonObject.getJSONObject("payload");
                if (payload.has("commits")) {
                    JSONArray commits = payload.getJSONArray("commits");
                    for (int j = 0; j < commits.length(); j++) {
                        JSONObject singleCommit = commits.getJSONObject(j);
                        JSONObject author = singleCommit.getJSONObject("author");
                        String username = jsonObject.getJSONObject("actor").getString("display_login");
                        if (username.toLowerCase().equals(user.toLowerCase())) {
                            String userEmail = author.getString("email");
                            if (!emailsList.contains(userEmail))
                                emailsList.add(userEmail);
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            emailsList.add(errorMessage);
            emailsList.add(exceptionMessage);
            return emailsList;
        }
        finally {
            responseUserEmails.close();
        }
        return emailsList;
    }

    public List<String> getUserRepos(){
        List<String> repoList = new ArrayList<>();
        Response responseUserRepos = null;
        try {
            APIRequest apiRequest = new APIRequest("users/"+ user +"/repos");
            responseUserRepos = this.newCall(apiRequest.getRequest()).execute();
            if(!responseUserRepos.isSuccessful()){
                repoList.add(errorMessage);
                repoList.add(responseIssue);
                return repoList;
            }

            String stringUserRepos = responseUserRepos.body().string();
            JSONArray jsonUserRepos = new JSONArray(stringUserRepos);
            for (int i =0; i< jsonUserRepos.length(); i++){
                repoList.add((String) jsonUserRepos.getJSONObject(i).get("name"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            repoList.add(errorMessage);
            repoList.add(exceptionMessage);
            return repoList;
        }
        finally {
            responseUserRepos.close();
        }
        return repoList;
    }

    public List<String> languageStatistics(List<String> repos){
        HashMap<String, Integer> statisticMap = new HashMap<>();
        Response responseLanguageStatistics = null;
        Integer totalBytes = 0;
        for(String repoName: repos)
            try {
                APIRequest apiRequest = new APIRequest("repos/"+ user +"/" + repoName + "/languages");
                responseLanguageStatistics = this.newCall(apiRequest.getRequest()).execute();
                if(!responseLanguageStatistics.isSuccessful()){
                    List<String> errorArray = new ArrayList<String>();
                    errorArray.add(errorMessage);
                    errorArray.add(responseIssue);
                    return errorArray;
                }

                String repoLanguages = responseLanguageStatistics.body().string();
                JSONObject jsonRepoLanguages = new JSONObject(repoLanguages);
                Iterator<?> keys = jsonRepoLanguages.keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    Integer repoValue = (Integer)jsonRepoLanguages.get(key);
                    totalBytes += repoValue;
                    if (statisticMap.containsKey(key)) {
                        Integer byteAmount = statisticMap.get(key) + repoValue;
                        statisticMap.put(key, byteAmount);
                    }else{
                        statisticMap.put(key, repoValue);
                    }
                }
                statisticMap.put(stringTotalBytes, totalBytes);
            } catch (JSONException | IOException e1) {
                e1.printStackTrace();
                List<String> errorArray = new ArrayList<String>();
                errorArray.add(errorMessage);
                errorArray.add(exceptionMessage);
                return errorArray;
            }
            finally {
                responseLanguageStatistics.close();
            }
        return computeStatistic(statisticMap);
    }

    public List<String> computeStatistic(HashMap<String, Integer> statisticMap){
        Integer totalBytes = statisticMap.get(stringTotalBytes);
        statisticMap.remove(stringTotalBytes);
        Iterator it = statisticMap.entrySet().iterator();
        List<String> languageStatistic = new ArrayList<String>();
        while (it.hasNext()){
            HashMap.Entry mapEntry = (HashMap.Entry)it.next();
            Integer temp = (Integer)mapEntry.getValue();
            double languagePercentage = (double)temp/totalBytes;
            String stringLanguage = mapEntry.getKey() + ": " + String.format("%.2g",languagePercentage) + "%";
            languageStatistic.add(stringLanguage);
        }
        return languageStatistic;
    }
}
