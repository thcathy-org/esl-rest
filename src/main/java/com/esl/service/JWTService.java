package com.esl.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
public class JWTService {
    private static Logger log = LoggerFactory.getLogger(JWTService.class);

    private PublicKey key;

    @Value(value = "${auth0.cert}")
    private String certificatePath;

    @PostConstruct
    public void init() {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is =  this.getClass().getResourceAsStream(certificatePath);
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            this.key = cer.getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            return Optional.of(
                    Jwts.parser().setSigningKey(key)
                            .parseClaimsJws(token.replace("Bearer", "").replaceAll("\"", ""))
                            .getBody()
            );
        } catch (Exception e) {
            log.warn("Fail to parse clamis", e);
            return Optional.empty();
        }
    }

    public Optional<String> parseEmail(String token) {
        try {
            Claims claims = parseClaims(token).get();
            return Optional.of(claims.get("email", String.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
