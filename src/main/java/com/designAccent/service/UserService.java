package com.designAccent.service;

import com.designAccent.dto.UserDto;
import com.designAccent.entity.Role;
import com.designAccent.entity.User;
import com.designAccent.exception.BadRequestException;
import com.designAccent.exception.ResourceNotFoundException;
import com.designAccent.exception.UnauthorizedException;
import com.designAccent.repository.RoleRepository;
import com.designAccent.repository.UserRepository;
import com.designAccent.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // LOGIN
    public UserDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Invalid email or password"));

        // verify BCrypt password
        if (!passwordEncoder.matches(
                password, user.getPassword())) {
            throw new UnauthorizedException(
                    "Invalid email or password");
        }

        // generate token with email only
        String token = jwtUtil.generateToken(
                user.getEmail());

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setContactNo(user.getContactNo());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setToken(token);

        if (user.getRole() != null) {
            dto.setRole(user.getRole().getRole());
        }

        return dto;
    }

    // CREATE USER
    public UserDto createUser(UserDto request) {
        if (request.getName() == null ||
                request.getName().isEmpty()) {
            throw new BadRequestException(
                    "Name is required");
        }
        if (request.getEmail() == null ||
                request.getEmail().isEmpty()) {
            throw new BadRequestException(
                    "Email is required");
        }
        if (request.getPassword() == null ||
                request.getPassword().isEmpty()) {
            throw new BadRequestException(
                    "Password is required");
        }

        // check duplicate email
        if (userRepository.findByEmail(
                request.getEmail()).isPresent()) {
            throw new BadRequestException(
                    "Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // encode password with BCrypt
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        user.setContactNo(request.getContactNo());
        user.setAddress(request.getAddress());
        user.setCreatedAt(LocalDateTime.now());

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(
                    Long.valueOf(request.getRoleId()))
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Role not found"));
            user.setRole(role);
        }

        User saved = userRepository.save(user);

        UserDto dto = new UserDto();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setEmail(saved.getEmail());
        return dto;
    }
}