package com.designAccent.controller;

import com.designAccent.dto.ApiResponse;
import com.designAccent.dto.UserDto;
import com.designAccent.entity.User;
import com.designAccent.exception.ResourceNotFoundException;
import com.designAccent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/api/users/update/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        if (request.get("name") != null) {
            user.setName((String) request.get("name"));
        }
        if (request.get("email") != null) {
            user.setEmail((String) request.get("email"));
        }
        if (request.get("contactNo") != null) {
            user.setContactNo(
                    (String) request.get("contactNo"));
        }
        if (request.get("address") != null) {
            user.setAddress(
                    (String) request.get("address"));
        }
        if (request.get("password") != null &&
                !((String) request.get("password"))
                        .isEmpty()) {
            // encode password with BCrypt
            user.setPassword(
                    passwordEncoder.encode(
                            (String) request.get("password")));
        }

        User saved = userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setEmail(saved.getEmail());
        dto.setContactNo(saved.getContactNo());
        dto.setAddress(saved.getAddress());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated successfully", dto));
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setContactNo(user.getContactNo());
        dto.setAddress(user.getAddress());

        return ResponseEntity.ok(
                ApiResponse.success("User found", dto));
    }

    @PostMapping("/api/users/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody Map<String, Object> request) {

        String email =
                (String) request.get("email");
        String newPassword =
                (String) request.get("newPassword");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Email is required"));
        }
        if (newPassword == null ||
                newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Password must be at least 6 characters"));
        }

        Optional<User> userOpt =
                userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "No account found with this email"));
        }

        User user = userOpt.get();
        // encode password with BCrypt before saving
        user.setPassword(
                passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully", null));
    }
}