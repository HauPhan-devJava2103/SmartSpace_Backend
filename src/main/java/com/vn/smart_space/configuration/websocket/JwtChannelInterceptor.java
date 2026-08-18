package com.vn.smart_space.configuration.websocket;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.service.jwt.IJwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "WS-AUTH")
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final IJwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        // Authenticate client
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                SignedJWT signedJWT = jwtService.verifyToken(token);
                String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
                if (jwtService.isTokenBlacklisted(jwtId)) {
                    throw new MessagingException("Token has been revoked");
                }

                // only allow access token
                String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("tokenType");
                if (!"access".equals(tokenType)) {
                    throw new MessagingException("Only access tokens are accepted for WebSocket");
                }

                String email = signedJWT.getJWTClaimsSet().getSubject();
                String userId = (String) signedJWT.getJWTClaimsSet().getClaim("userId");
                String scope = (String) signedJWT.getJWTClaimsSet().getClaim("scope");

                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(scope));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email,
                        null,
                        authorities);

                accessor.setUser(authentication);
                accessor.getSessionAttributes().put("userId", userId);

                log.info("WebSocket CONNECT authenticated: email={}, userId={}", email, userId);
            } catch (Exception e) {
                throw new MessagingException("Failed to parse JWT token: " + e.getMessage());
            }

        }
        return message;
    }
}
