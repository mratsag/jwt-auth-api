package com.codelabtv.jwt_auth_api.service;

import com.codelabtv.jwt_auth_api.dto.*;
import com.codelabtv.jwt_auth_api.entity.RefreshToken;
import com.codelabtv.jwt_auth_api.entity.Role;
import com.codelabtv.jwt_auth_api.entity.User;
import com.codelabtv.jwt_auth_api.repository.UserRepository;
import com.codelabtv.jwt_auth_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public String register(RegisterRequest request){
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        userRepository.save(user);

        return "User registered successfully";
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String accessToken = jwtUtil.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                user.getUsername(),
                "Login successful"
        );
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtUtil.generateToken(user.getUsername());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getUsername());

                    return new RefreshTokenResponse(
                            accessToken,
                            newRefreshToken.getToken(),
                            "Refresh successful"
                    );
                })
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public String logout(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        refreshTokenService.deleteByUserId(user.getId());

        return "Logout successful";
    }


}
