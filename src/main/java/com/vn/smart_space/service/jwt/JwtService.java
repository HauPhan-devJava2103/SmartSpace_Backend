package com.vn.smart_space.service.jwt;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.exception.UnauthorizedException;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.InvalidatedTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "JWT-SERVICE")
@RequiredArgsConstructor
public class JwtService implements IJwtService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerKey;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    public TokenPayload generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiryTime = new Date(Instant.ofEpochMilli(issueTime.getTime())
                .plus(10, ChronoUnit.MINUTES)
                .toEpochMilli());
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("smartspace.vn")
                .issueTime(issueTime)
                .expirationTime(expiryTime)
                .jwtID(jwtId)
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .claim("tokenType", "access")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            String token = jwsObject.serialize();
            return TokenPayload.builder()
                    .token(token)
                    .jwtId(jwtId)
                    .expiryTime(expiryTime)
                    .build();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException("Can not generate token", e);
        }
    }

    @Override
    public TokenPayload generateRefreshToken(User user, boolean rememberMe) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiryTime = new Date(Instant.ofEpochMilli(issueTime.getTime())
                .plus(rememberMe ? 30 : 1, ChronoUnit.DAYS)
                .toEpochMilli());
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("smartspace.vn")
                .issueTime(issueTime)
                .expirationTime(expiryTime)
                .jwtID(jwtId)
                .claim("tokenType", "refresh")
                .claim("rememberMe", rememberMe)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            String token = jwsObject.serialize();
            return TokenPayload.builder()
                    .token(token)
                    .jwtId(jwtId)
                    .expiryTime(expiryTime)
                    .build();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException("Can not generate token", e);
        }
    }

    @Override
    public SignedJWT verifyToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            var verified = signedJWT.verify(verifier);

            if (!(verified && expiryTime.after(new Date())))
                throw new UnauthorizedException("Token is invalid or expired");

            return signedJWT;
        } catch (JOSEException | ParseException e) {
            throw new UnauthorizedException("Token is invalid");
        }
    }

    @Override
    public String buildScope(User user) {
        if (user.getRole() != null) {
            return "ROLE_" + user.getRole().name();
        }
        return "ROLE_USER";
    }

    @Override
    public JwtInfo parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            return JwtInfo.builder()
                    .jwtId(jwtId)
                    .issueTime(issueTime)
                    .expiryTime(expiryTime)
                    .build();

        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String jwtId) {
        return invalidatedTokenRepository.existsById(jwtId);
    }
}
