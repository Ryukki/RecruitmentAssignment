package com.jakubwidlak.assignment.businesslogic;

import com.jakubwidlak.assignment.dataprovider.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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
        httpClient.setUser(username);
        model.addAttribute("username", this.httpClient.getUser());
        model.addAttribute("userEmails", this.httpClient.getUserEmails());
        List<String> userRepos = this.httpClient.getUserRepos();
        model.addAttribute("userRepos", userRepos);
        model.addAttribute("languageStatistic", this.httpClient.languageStatistics(userRepos));

        return "searchresults";
    }
}
