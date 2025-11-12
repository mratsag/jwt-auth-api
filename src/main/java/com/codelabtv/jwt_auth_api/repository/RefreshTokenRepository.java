package com.codelabtv.jwt_auth_api.repository;

import com.codelabtv.jwt_auth_api.entity.RefreshToken;
import com.codelabtv.jwt_auth_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);
}