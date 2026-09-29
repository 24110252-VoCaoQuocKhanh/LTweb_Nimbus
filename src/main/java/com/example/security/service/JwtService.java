package com.example.security.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.security.exception.JwtTokenException;
import com.example.security.exception.JwtTokenExpiredException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    private static final long CLOCK_SKEW_MILLIS = 60_000;

    private final byte[] secretKey;
    private final long expirationTime;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-time}") long expirationTime,
            @Value("${security.jwt.issuer}") String issuer) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret phải có ít nhất 32 byte cho HS256");
        }
        this.secretKey = keyBytes.clone();
        this.expirationTime = expirationTime;
        this.issuer = issuer;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + expirationTime))
                .claim("authorities", authorities)
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .type(JOSEObjectType.JWT)
                        .build(),
                claims);
        try {
            signedJwt.sign(new MACSigner(secretKey));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new JwtTokenException("Không thể ký JWT bằng Nimbus", exception);
        }
    }

    public String extractUsername(String token) {
        return parseAndValidate(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        JWTClaimsSet claims = parseAndValidate(token);
        return userDetails.getUsername().equalsIgnoreCase(claims.getSubject())
                && issuer.equals(claims.getIssuer())
                && claims.getExpirationTime() != null
                && claims.getExpirationTime().after(new Date());
    }

    public JWTClaimsSet parseAndValidate(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(signedJwt.getHeader().getAlgorithm())) {
                throw new JwtTokenException("Thuật toán ký JWT không được hỗ trợ");
            }
            if (!signedJwt.verify(new MACVerifier(secretKey))) {
                throw new JwtTokenException("Chữ ký JWT không hợp lệ");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            validateClaims(claims);
            return claims;
        } catch (ParseException | JOSEException exception) {
            throw new JwtTokenException("JWT không đúng định dạng", exception);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        Date now = new Date();
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || !expirationTime.after(now)) {
            throw new JwtTokenExpiredException("JWT đã hết hạn");
        }
        if (!issuer.equals(claims.getIssuer())) {
            throw new JwtTokenException("Issuer của JWT không hợp lệ");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtTokenException("JWT thiếu subject");
        }
        Date issueTime = claims.getIssueTime();
        if (issueTime == null || issueTime.getTime() > now.getTime() + CLOCK_SKEW_MILLIS) {
            throw new JwtTokenException("Thời điểm phát hành JWT không hợp lệ");
        }
    }

    public long getExpirationTime() {
        return expirationTime;
    }
}
