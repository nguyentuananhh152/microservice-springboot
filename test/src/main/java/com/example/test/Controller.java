package com.example.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@CrossOrigin
public class Controller {
	@GetMapping()
	public ResponseEntity<String> test() {
		System.out.println("Call api");
		return ResponseEntity.badRequest().body("Test call api");
	}
}
