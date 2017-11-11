package com.jakubwidlak.assignment.businesslogic;

import com.jakubwidlak.assignment.dataprovider.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * Created by Ryukki on 06.11.2017.
 */
@org.springframework.stereotype.Controller
public class Controller {
    @Autowired
    private HttpClient httpClient;

    @RequestMapping(value = "/searchresults", method = RequestMethod.GET)
    public String searchResults(@RequestParam(value = "username", required = false, defaultValue = "")String username, Model model){
        ResponseObject responseObject = new ResponseObject();
        if(username.equals("")){
            responseObject.setUsername("Please provide username before pressing \" Search \".");
        }
        else{
            if(httpClient.setUser(username)){
                responseObject.setUserEmails(this.httpClient.getUserEmails());
                List<String> userRepos = this.httpClient.getUserRepos();
                responseObject.setUserRepos(userRepos);
                responseObject.setLanguageStatistic(this.httpClient.languageStatistics(userRepos));
            }
            responseObject.setUsername(this.httpClient.getUser());
        }
        model.addAttribute("responseObject", responseObject);
        return "searchresults";
    }
}
