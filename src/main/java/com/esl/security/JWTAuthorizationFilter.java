package com.esl.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private PublicKey key;
    private boolean isTesting;

    public JWTAuthorizationFilter(AuthenticationManager authManager, String certificatePath, boolean isTesting) {
        super(authManager);
        this.isTesting = isTesting;

        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is =  this.getClass().getResourceAsStream(certificatePath);
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            this.key = cer.getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (token != null) {
                // parse the token.
                Claims claims = Jwts.parser().setSigningKey(key)
                        .parseClaimsJws(token.replace("Bearer", "").replaceAll("\"", ""))
                        .getBody();

                String user = claims.get("email", String.class);

                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, null);
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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