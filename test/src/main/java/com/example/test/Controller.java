package com.example.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Controller {
	@GetMapping()
	public ResponseEntity<String> test() {
		System.out.println("Call api");
		return ResponseEntity.ok().body("Test call api");
	}
}
