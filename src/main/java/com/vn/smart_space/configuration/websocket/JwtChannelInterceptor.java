package com.vn.smart_space.configuration.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "WS-AUTH")
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                AbstractAuthenticationToken auth = jwtAuthenticationConverter.convert(jwt);

                accessor.setUser(auth);

                String userId = jwt.getClaimAsString("userId");
                if (accessor.getSessionAttributes() != null && userId != null) {
                    accessor.getSessionAttributes().put("userId", userId);
                }

                log.info("WebSocket CONNECT authenticated: user={}", jwt.getSubject());
            } catch (Exception e) {
                log.error("WebSocket Authentication failed: {}", e.getMessage());
                throw new MessagingException("Unauthorized: " + e.getMessage());
            }
        }
        return message;
    }
}