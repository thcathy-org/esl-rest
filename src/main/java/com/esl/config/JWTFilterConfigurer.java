package com.esl.config;

import com.esl.security.JWTAuthorizationFilter;
import com.esl.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public class JWTFilterConfigurer extends AbstractHttpConfigurer<JWTFilterConfigurer, HttpSecurity> {
    private boolean isTesting;
    private JWTService jwtService;

    public JWTFilterConfigurer(JWTService jwtService, boolean isTesting) {
        this.isTesting = isTesting;
        this.jwtService = jwtService;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new JWTAuthorizationFilter(authenticationManager, jwtService, isTesting));
    }

}
