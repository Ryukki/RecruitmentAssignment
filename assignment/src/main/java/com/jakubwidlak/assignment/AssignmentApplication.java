package com.jakubwidlak.assignment;

import com.jakubwidlak.assignment.dataprovider.HttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class AssignmentApplication {

	public static void main(String[] args) {
		/*HttpClient httpClient = new HttpClient();
		List<String> userRepos = httpClient.getUserRepos();
		httpClient.languageStatistics(userRepos);*/
		SpringApplication.run(AssignmentApplication.class, args);
	}
}
