package com.example.secservcei.service.impl;

import com.example.secservcei.service.AuthenticationService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService implements AuthenticationService {

    private JwtEncoder jwtEncoder;
    private UserDetailsService userDetailsService;

    public AuthService(JwtEncoder jwtEncoder, UserDetailsService userDetailsService) {
        this.jwtEncoder = jwtEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Map<String, String> generateToken(String username, boolean withRefreshToken) {
        Map<String, String> idToken = new HashMap<>();
        Instant instant = Instant.now();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String scope = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority())
                .collect(Collectors.joining(" "));
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(username)
                .issuer("http://localhost:8088/auth-service")
                .expiresAt(instant.plus(withRefreshToken ? 5 : 30, ChronoUnit.MINUTES))
                .issuedAt(instant)
                .claim("scope", scope)
                .build();
        String jwtAccessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
        idToken.put("access-token", jwtAccessToken);
        if (withRefreshToken) {
            JwtClaimsSet jwtRefreshClaimsSet = JwtClaimsSet.builder()
                    .subject(username)
                    .issuer("http://localhost:8088/auth-service")
                    .expiresAt(instant.plus(30, ChronoUnit.MINUTES))
                    .issuedAt(instant)
                    .build();
            String jwtRefreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtRefreshClaimsSet)).getTokenValue();
            idToken.put("refresh-token", jwtRefreshToken);
        }
        return idToken;
    }
}
