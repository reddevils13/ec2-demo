package com.example.docker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	@Value("${RDS_URL}")
	private String RDS_URL;
	
	@GetMapping("/")
	public String home() {
		return "Welcome to Docker World!";
	}
	
	@GetMapping("/get")
	public String getEnv() {
		return RDS_URL;
	}
	
}
