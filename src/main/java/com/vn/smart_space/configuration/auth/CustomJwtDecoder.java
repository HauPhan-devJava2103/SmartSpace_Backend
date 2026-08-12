package com.vn.smart_space.configuration.auth;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.service.auth.AuthenticationService;
import com.vn.smart_space.service.auth.IAuthenticationService;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    private final IAuthenticationService authenticationService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        var response = authenticationService.introspect(
                IntrospectRequest.builder()
                        .token(token)
                        .build());

        if (!response.isValid()) {
            throw new JwtException("Token invalid");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("tokenType");
            if (!"access".equals(tokenType)) {
                throw new JwtException("Only access tokens are accepted");
            }
        } catch (ParseException e) {
            throw new JwtException("Token invalid");
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");

            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }

}
