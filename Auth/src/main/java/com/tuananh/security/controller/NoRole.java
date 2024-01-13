package com.tuananh.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test/no-role")
@CrossOrigin
public class NoRole {
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok().body("Call api ok");
    }
}
