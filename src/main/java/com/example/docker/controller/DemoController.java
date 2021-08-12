package com.example.docker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	@Value("${RDS_URL}")
	private String RDS_URL;
	
	@Value("${RDS_PASSWORD}")
	private String RDS_PASSWORD;
	
	@Value("${RDS_USERNAME}")
	private String RDS_USERNAME;
	
	
	@GetMapping("/")
	public String home() {
		return "Welcome to Docker World!";
	}
	
	@GetMapping("/var")
	public String getEnv() {
		return RDS_PASSWORD+" "+RDS_URL+" "+RDS_USERNAME;
	}
	
}
