package com.jakubwidlak.assignment.dataprovider;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class HttpClient extends OkHttpClient {
    private static final String errorMessage = "Ooops, something went wrong :( ";
    private static final String responseIssue = "Please check GitHub API status.";
    private static final String exceptionMessage = "Contact app provider.";
    private static final String stringTotalBytes= "totalBytes";

    private String user;
    private APIRequest apiRequest;
    private Response response;

    public  HttpClient(){
        apiRequest = new APIRequest();
    }

    public boolean setUser(String user) {
        this.user = user;
        try {
            response = this.newCall(apiRequest.buildRequest("users/" + user)).execute();
            if (!response.isSuccessful()) {
                this.user = "Sorry user might not exist. Please check GitHub API status.";
                return false;
            }
        } catch (IOException e) {
            this.user = "Sorry user might not exist. Please check GitHub API status.";
            e.printStackTrace();
            return false;
        } finally {
            response.close();
        }
        return true;
    }

    public String getUser() {
        return user;
    }

    public List<String> getUserEmails(){
        List<String> emailsList = new ArrayList<>();
        try {
            response = this.newCall(apiRequest.buildRequest("users/" + user + "/events")).execute();
            if(!response.isSuccessful()){
                emailsList.add(errorMessage);
                emailsList.add(responseIssue);
                return emailsList;
            }
            String stringUserEmails = response.body().string();
            emailsList = readEmailJSONArray(stringUserEmails);
        } catch (IOException e) {
            e.printStackTrace();
            emailsList.add(errorMessage);
            emailsList.add(exceptionMessage);
            return emailsList;
        }
        finally {
            response.close();
        }
        return emailsList;
    }

    public List<String> readEmailJSONArray(String stringUserEmails){
        List<String> emailsList = new ArrayList<>();
        JSONArray jsonUserEmails = null;
        try {
            jsonUserEmails = new JSONArray(stringUserEmails);
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
        } catch (JSONException e) {
            e.printStackTrace();
            emailsList.add(errorMessage);
            emailsList.add(exceptionMessage);
            return emailsList;
        }
        if(emailsList.isEmpty()){
            emailsList.add("Sorry, we couldn't find any emails.");
        }
        return  emailsList;
    }

    public List<String> getUserRepos(){
        List<String> repoList = new ArrayList<>();
        try {
            response = this.newCall(apiRequest.buildRequest("users/"+ user +"/repos")).execute();
            if(!response.isSuccessful()){
                repoList.add(errorMessage);
                repoList.add(responseIssue);
                return repoList;
            }

            String stringUserRepos = response.body().string();
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
            response.close();
        }
        return repoList;
    }

    public List<String> languageStatistics(List<String> repos){
        HashMap<String, Integer> statisticMap = new HashMap<>();
        Integer totalBytes = 0;
        for(String repoName: repos)
            try {
                response = this.newCall(apiRequest.buildRequest("repos/"+ user +"/" + repoName + "/languages")).execute();
                if(!response.isSuccessful()){
                    List<String> errorArray = new ArrayList<>();
                    errorArray.add(errorMessage);
                    errorArray.add(responseIssue);
                    return errorArray;
                }

                String repoLanguages = response.body().string();
                JSONObject jsonRepoLanguages = new JSONObject(repoLanguages);
                Iterator<?> keys = jsonRepoLanguages.keys();
                while(keys.hasNext()) {
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
                List<String> errorArray = new ArrayList<>();
                errorArray.add(errorMessage);
                errorArray.add(exceptionMessage);
                return errorArray;
            }
            finally {
                response.close();
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
