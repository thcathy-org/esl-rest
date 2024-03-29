package com.esl.security;

import com.esl.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    public static String TESTING_HEADER = "email";
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
                var claims = jwtService.parseClaims(token).orElseThrow();
                String email = claims.get("email", String.class);
                if (Strings.isNotBlank(email)) {
                    var authorities = claims.keySet().stream().filter(k -> k.endsWith("roles")).findFirst()
                            .map(k -> (ArrayList<String>) claims.get(k, ArrayList.class))
                            .map(l -> l.stream().map(v -> new SimpleGrantedAuthority("ROLE_" + v)).collect(Collectors.toList()));
                    return new UsernamePasswordAuthenticationToken(email, null, authorities.orElse(null));
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
        String header = req.getHeader(TESTING_HEADER);
        if (isTesting && header != null ) {
            String userId = header.trim();
            return new UsernamePasswordAuthenticationToken(userId, null, null);
        }
        return null;
    }
}
