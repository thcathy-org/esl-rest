package com.esl.security;

import com.esl.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private static Logger log = LoggerFactory.getLogger(JWTService.class);
    private boolean isTesting;
    private JWTService jwtService;

    public JWTAuthorizationFilter(AuthenticationManager authManager, JWTService jwtService, boolean isTesting) {
        super(authManager);
        this.isTesting = isTesting;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        if (noAuthorizationHeader(req) && !isTesting) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private boolean noAuthorizationHeader(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header == null || !header.startsWith("Bearer");
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userToken = isTestingWithToken(request);
        if (userToken != null) return userToken;

        try {
            String token = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(token)) {
                Optional<String> email = jwtService.parseEmail(token);
                if (email.isPresent()) {
                    return new UsernamePasswordAuthenticationToken(email.get(), null, null);
                }
            }
        } catch (ExpiredJwtException e) {
            log.info(e.toString());
        } catch (Exception e) {
            log.error("Error in processing JWT:", e);
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken isTestingWithToken(HttpServletRequest req) {
        String header = req.getHeader("email");
        if (isTesting && header != null ) {
            String userId = header.trim();
            return new UsernamePasswordAuthenticationToken(userId, null, null);
        }
        return null;
    }
}