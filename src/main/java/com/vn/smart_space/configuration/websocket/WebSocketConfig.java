package com.vn.smart_space.configuration.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Broker Prefix
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(this.messageBrokerTaskScheduler);

        // Application Prefix
        registry.setApplicationDestinationPrefixes("/smartspace");

        // User Destination Prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Endpoint for Web
        registry.addEndpoint("/ws-socketjs")
                .setAllowedOriginPatterns("http://localhost:*").withSockJS();

        // Endpoint for Mobile
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:*", "*");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registry interceptor authenticate JWT STOMP
        registration.interceptors(jwtChannelInterceptor);
    }

}
