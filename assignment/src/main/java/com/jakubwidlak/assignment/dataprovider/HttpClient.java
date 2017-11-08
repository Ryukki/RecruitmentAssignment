package com.jakubwidlak.assignment.dataprovider;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap;
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



    private String user;
    private String errorMessage = "Ooops, something went wrong :(";
    private static final String stringTotalBytes= "totalBytes";

    public void setUser(String user) {
        this.user = user;
    }
    public String getUser() {
        return user;
    }

    public List<String> getUserEmails(){
        List<String> emailsList = new ArrayList<>();
        Response responseUserEmails = null;
        try {
            APIRequest apiRequest = new APIRequest(user +"/emails");
            responseUserEmails = this.newCall(apiRequest.getRequest()).execute();
            if(!responseUserEmails.isSuccessful()){
                emailsList.add(errorMessage);
                return emailsList;
            }

            //When information wasn't found the response is a JSON object not JSON Array
            String stringUserEmails = responseUserEmails.body().string();
            if(!stringUserEmails.startsWith("[")){
                emailsList.add(errorMessage);
                return emailsList;
            }
            JSONArray jsonUserEmails = new JSONArray(stringUserEmails);
            for (int i =0; i< jsonUserEmails.length(); i++){
                emailsList.add((String) jsonUserEmails.getJSONObject(i).get("email"));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
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
                return repoList;
            }

            String stringUserRepos = responseUserRepos.body().string();
            JSONArray jsonUserRepos = new JSONArray(stringUserRepos);
            for (int i =0; i< jsonUserRepos.length(); i++){
                repoList.add((String) jsonUserRepos.getJSONObject(i).get("name"));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            responseUserRepos.close();
        }
        return repoList;
    }

    public Map<String, Integer> languageStatistics(List<String> repos){
        HashMap<String, Integer> statisticMap = new HashMap<>();
        Response responseLanguageStatistics = null;
        Integer totalBytes = 0;
        for(String repoName: repos)
            try {
                APIRequest apiRequest = new APIRequest("repos/"+ user +"/" + repoName + "/languages");
                responseLanguageStatistics = this.newCall(apiRequest.getRequest()).execute();
                if(!responseLanguageStatistics.isSuccessful()){
                    statisticMap.put(errorMessage, 1);
                    return statisticMap;
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
            }
            finally {
                responseLanguageStatistics.close();
            }

        return computeStatistic(statisticMap);
    }

    public Map<String, Integer> computeStatistic(HashMap<String, Integer> statisticMap){
        Integer totalBytes = statisticMap.get(stringTotalBytes);
        /*Iterator it = statisticMap.entrySet().iterator();
        while (it.hasNext()){
            HashMap.Entry mapEntry = (HashMap.Entry)it.next();
            if(mapEntry.getKey()!= stringTotalBytes){
                double languagePercentage = (Integer)mapEntry.getValue()/totalBytes;

            }
        }*/
        statisticMap.replaceAll((k, v)-> v/totalBytes);
        statisticMap.remove(stringTotalBytes);
        return statisticMap;
    }

}
