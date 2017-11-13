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

    /**
     * Checks if given user exist and sets corresponding field.
     * @param username user being checked and set as a field value
     * @return true if user exist
     */
    public boolean setUser(String username) {
        this.user = "Sorry user might not exist. Please check GitHub API status.";
        try {
            response = this.newCall(apiRequest.buildRequest("users/" + username)).execute();
            if (!response.isSuccessful()) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            response.close();
        }
        this.user = username;
        return true;
    }

    public String getUser() {
        return user;
    }

    /**
     * Builds array with error information.
     * @param errorCase reason of an error
     * @return array with error information
     */
    private List<String> makeErrorArray(int errorCase){
        List<String> errorArray = new ArrayList<>();
        errorArray.add(errorMessage);
        if(errorCase == 0)
            errorArray.add(exceptionMessage);
        else if (errorCase == 1)
            errorArray.add(responseIssue);
        return errorArray;
    }

    /**
     * Requests list of user events which is used to get their emails.
     * @return list of user email addresses
     */
    public List<String> getUserEmails(){
        List<String> emailsList;
        try {
            response = this.newCall(apiRequest.buildRequest("users/" + user + "/events")).execute();
            if(!response.isSuccessful() && response.body()!=null){
                return makeErrorArray(1);
            }
            String stringUserEmails = response.body().string();
            emailsList = readEmailJSONArray(stringUserEmails);
        } catch (IOException e) {
            e.printStackTrace();
            return makeErrorArray(0);
        }
        finally {
            response.close();
        }
        return emailsList;
    }

    /**
     * Extracts user emails from response string.
     * @param stringUserEmails api response string
     * @return list of user email addresses
     */
    private List<String> readEmailJSONArray(String stringUserEmails){
        List<String> emailsList = new ArrayList<>();
        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
            return makeErrorArray(0);
        }
        if(emailsList.isEmpty()){
            emailsList.add("Sorry, we couldn't find any emails.");
        }
        return  emailsList;
    }

    /**
     * Requests and processes list of user repositories.
     * @return list of user repositories
     */
    public List<String> getUserRepos(){
        List<String> repoList = new ArrayList<>();
        try {
            response = this.newCall(apiRequest.buildRequest("users/"+ user +"/repos")).execute();
            if(!response.isSuccessful() && response.body()!=null){
                return makeErrorArray(1);
            }

            String stringUserRepos = response.body().string();
            JSONArray jsonUserRepos = new JSONArray(stringUserRepos);
            for (int i =0; i< jsonUserRepos.length(); i++){
                repoList.add((String) jsonUserRepos.getJSONObject(i).get("name"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return makeErrorArray(0);
        }
        finally {
            response.close();
        }
        return repoList;
    }

    /**
     * Requests languages for every user repository.
     * @param repos list of user repositories
     * @return list with language statistics
     */
    public List<String> languageStatistics(List<String> repos){
        HashMap<String, Integer> statisticMap = new HashMap<>();
        Integer totalBytes = 0;
        for(String repoName: repos)
            try {
                response = this.newCall(apiRequest.buildRequest("repos/"+ user +"/" + repoName + "/languages")).execute();
                if(!response.isSuccessful() && response.body()!=null){
                    return makeErrorArray(1);
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
                return makeErrorArray(0);
            }
            finally {
                response.close();
            }
        return computeStatistic(statisticMap);
    }

    /**
     * Transforms Map to list of strings with percentage statistics.
     * @param statisticMap Map with languages information
     * @return list of percentage statistics
     */
    private List<String> computeStatistic(HashMap<String, Integer> statisticMap){
        Integer totalBytes = statisticMap.get(stringTotalBytes);
        statisticMap.remove(stringTotalBytes);
        Iterator it = statisticMap.entrySet().iterator();
        List<String> languageStatistic = new ArrayList<>();
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