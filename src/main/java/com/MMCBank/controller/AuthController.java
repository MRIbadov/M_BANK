package com.MMCBank.controller;

import com.MMCBank.dto.*;
import com.MMCBank.entity.User;
import com.MMCBank.repository.UserRepository;
import com.MMCBank.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager, UserRepository userRepo,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authManager     = authManager;
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            User user = (User) auth.getPrincipal();
            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(
                    token, user.getUsername(),
                    user.getFirstName() + " " + user.getLastName()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already taken"));
        }
        User user = new User();
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setEmail(req.email());
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));  // BCrypt
        user.setPhoneNumber(req.phoneNumber());
        userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Account created. Please log in."));
    }
}
