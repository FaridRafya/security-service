package com.example.secservcei.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    private RsaKeyConfig rsaKeyConfig;
    private PasswordEncoder passwordEncoder;

    public SecurityConfig(RsaKeyConfig rsaKeyConfig, PasswordEncoder passwordEncoder) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.passwordEncoder = passwordEncoder;
    }


    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }
    @Bean
    public UserDetailsService userDetailsService(){
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(passwordEncoder.encode("1234")).authorities("USER").build(),
                User.withUsername("admin").password(passwordEncoder.encode("1234")).authorities("ADMIN").build(),
                User.withUsername("user3").password(passwordEncoder.encode("1234")).authorities("USER","ADMIN").build()
        );
    }


        @Bean
        public JwtEncoder jwtEncoder(){
            JWK jwk=new RSAKey.Builder(rsaKeyConfig.publicKey()).privateKey(rsaKeyConfig.privateKey()).build();
            JWKSource<SecurityContext> jwkSource=new ImmutableJWKSet<>(new JWKSet(jwk));
            return new NimbusJwtEncoder(jwkSource);
        }

        @Bean
        public JwtDecoder jwtDecoder(){

            return NimbusJwtDecoder.withPublicKey(rsaKeyConfig.publicKey()).build();
        }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf->csrf.disable())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests(ar->ar.requestMatchers("/messageTest","/public/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .httpBasic(Customizer.withDefaults())
                .build();

    }
}
