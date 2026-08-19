package com.skipcart.userservice.controller;

import com.skipcart.userservice.dto.AuthResponseDTO;
import com.skipcart.userservice.dto.UserLoginDTO;
import com.skipcart.userservice.dto.UserRegistrationDTO;
import com.skipcart.userservice.dto.UserResponseDTO;
import com.skipcart.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegistrationDTO dto) {
        UserResponseDTO response = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody UserLoginDTO dto) {
        AuthResponseDTO response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running!");
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        // We'll extract this from JWT context - just a placeholder to test protected route
        return ResponseEntity.ok("This is a protected endpoint - you have a valid token!");
    }
}