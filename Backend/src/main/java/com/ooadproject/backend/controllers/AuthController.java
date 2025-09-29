package com.ooadproject.backend.controllers;

import com.ooadproject.backend.dto.AuthRequestDTO;
import com.ooadproject.backend.dto.AuthResponseDTO;
import com.ooadproject.backend.dto.RegisterRequestDTO;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.services.UserService;
import com.ooadproject.backend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            User user = userService.registerUser(request); // Make sure your service uses request.getUsername()
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
