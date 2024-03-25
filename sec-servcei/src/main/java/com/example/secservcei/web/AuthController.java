package com.example.secservcei.web;

import com.example.secservcei.service.AuthenticationService;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {




    private AuthenticationService authenticationService;
    private AuthenticationManager authenticationManager;
    private JwtEncoder jwtEncoder;
    private JwtDecoder jwtDecoder;

    public AuthController(AuthenticationService authenticationService, AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @GetMapping("/messageTest")
   // @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public Map<String,Object> messageTest(Authentication authentication) {
        return Map.of("message","message testing securityy"
                ,"authentication",authentication);    }

    @PostMapping("/token")
    public Map<String,String> token(Authentication authentication){
        String username = authentication.getName();
        return authenticationService.generateToken(username,false);
    }

    @PostMapping("/public/auth")
    public ResponseEntity<Map<String,String>> auth(String granType, String username, String password, boolean withRefreshToken, String refreshToken){
        try {
            if (granType.equals("password")){
                Authentication authentication=authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username,password)
                );
                Map<String,String> idToken = authenticationService.generateToken(username,withRefreshToken);
                return new ResponseEntity<>(idToken, HttpStatus.OK);
            }else if (granType.equals("refreshToken")){
                if (refreshToken==null) return new ResponseEntity<>(Map.of("error message ","refreshToken is required"), HttpStatus.UNAUTHORIZED);
                Jwt jwt= jwtDecoder.decode(refreshToken);
                String  subject= jwt.getSubject();
                Map<String,String> idToken = authenticationService.generateToken(subject,withRefreshToken);
                return new ResponseEntity<>(idToken, HttpStatus.OK);
            }else
                return new ResponseEntity<>(Map.of("error message","graType n est suported"),HttpStatus.OK);
        }catch (Exception e){
            return   new ResponseEntity<>(Map.of("errorMessage",e.getMessage()),HttpStatus.UNAUTHORIZED);
        }
    }
}
