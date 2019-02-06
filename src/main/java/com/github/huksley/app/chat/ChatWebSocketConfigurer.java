package com.github.huksley.app.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.sockjs.transport.handler.WebSocketTransportHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/SorrySaury/WebChat/
 */
@Configuration
@EnableWebSocket
public class ChatWebSocketConfigurer implements WebSocketConfigurer {
    @Autowired
    private WebSocketHandler webSocketHandler;

    /*
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/api/chat/events", webSocketHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }*/

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.
            addHandler(webSocketHandler, "/api/chat/events").
            addInterceptors(new ChatHandShakeInterceptor()).
            // port 3000 if proxying through webpack-dev-server
            setAllowedOrigins("http://localhost, http://localhost:8087, http://localhost:8080, http://localhost:3000");

        //webSocketHandlerRegistry.addHandler(webSocketHandler, "/api/chat/events/sockjs").addInterceptors(new ChatHandShakeInterceptor()).withSockJS();
    }
}
