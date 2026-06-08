package com.designAccent.controller;

import com.designAccent.dto.ApiResponse;
import com.designAccent.dto.UserDto;
import com.designAccent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(
            @RequestBody Map<String, String> request) {

        String email    = request.get("email");
        String password = request.get("password");

        UserDto data = userService.login(email, password);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", data));
    }

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @RequestBody UserDto request) {

        UserDto data = userService.createUser(request);
        return ResponseEntity.ok(
                ApiResponse.success("User created", data));
    }
}