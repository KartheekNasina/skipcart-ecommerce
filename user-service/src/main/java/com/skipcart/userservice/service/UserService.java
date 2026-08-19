package com.skipcart.userservice.service;

import com.skipcart.userservice.dto.AuthResponseDTO;
import com.skipcart.userservice.dto.UserLoginDTO;
import com.skipcart.userservice.dto.UserRegistrationDTO;
import com.skipcart.userservice.dto.UserResponseDTO;
import com.skipcart.userservice.entity.User;
import com.skipcart.userservice.exception.InvalidCredentialsException;
import com.skipcart.userservice.exception.UserAlreadyExistsException;
import com.skipcart.userservice.repository.UserRepository;
import com.skipcart.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        log.info("Attempting to register user with email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new UserAlreadyExistsException("A user with this email already exists");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole() != null ? dto.getRole() : User.Role.CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        return UserResponseDTO.fromEntity(savedUser);
    }

    public AuthResponseDTO login(UserLoginDTO dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed - incorrect password for email: {}", dto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(
                user.getEmail(), user.getId(), user.getRole().name()
        );

        log.info("Login successful for email: {}", dto.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .user(UserResponseDTO.fromEntity(user))
                .build();
    }
}