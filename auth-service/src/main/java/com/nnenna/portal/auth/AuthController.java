package com.nnenna.portal.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record RegisterResponse(Long id, String email, String role) {}

    public record TokenResponse(String token, String tokenType, long expiresInSeconds) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserAccount saved = users.save(new UserAccount(email, encoder.encode(request.password()), "CUSTOMER"));
        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getRole());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = users.findByEmail(request.email().trim().toLowerCase())
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new TokenResponse(jwt.issue(user.getId(), user.getRole()), "Bearer", jwt.ttlSeconds());
    }
}
