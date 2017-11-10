package com.jakubwidlak.assignment.businesslogic;

import com.jakubwidlak.assignment.dataprovider.HttpClient;
import com.jakubwidlak.assignment.dataprovider.StringText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    /*public ModelAndView searchresults(@RequestParam(value = "username", required = false, defaultValue = "")String username){
        ModelAndView mav = new ModelAndView("searchresults");
        httpClient.setUser(username);
        ResponseObject responseObject = new ResponseObject();
        responseObject.setUsername(this.httpClient.getUser());
        responseObject.setUserEmails(this.httpClient.getUserEmails());
        List<String> userRepos = this.httpClient.getUserRepos();
        responseObject.setUserRepos(userRepos);
        responseObject.setLanguageStatistic(this.httpClient.languageStatistics(userRepos));
        mav.addObject("searchresults", responseObject);
        return mav;
    }*/
    public String searchResults(@RequestParam(value = "username", required = false, defaultValue = "")String username, Model model){
        httpClient.setUser(username);
        ResponseObject responseObject = new ResponseObject();
        responseObject.setUsername(this.httpClient.getUser());
        responseObject.setUserEmails(this.httpClient.getUserEmails());
        List<String> userRepos = this.httpClient.getUserRepos();
        responseObject.setUserRepos(userRepos);
        responseObject.setLanguageStatistic(this.httpClient.languageStatistics(userRepos));
        model.addAttribute("responseObject", responseObject);

        return "searchresults";
    }
}
