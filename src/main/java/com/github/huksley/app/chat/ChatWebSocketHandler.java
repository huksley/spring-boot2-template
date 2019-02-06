package com.github.huksley.app.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ChatUsers users;

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        log.info("afterConnectionEstablished, session {}", webSocketSession);
        users.addSession(webSocketSession);
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        log.info("handleMessage = {}", webSocketMessage);
        users.forEach((s) -> {
            try {
                log.info("Sending {} to {}", webSocketMessage, s);
                s.sendMessage(webSocketMessage);
            } catch (IOException e) {
                log.warn("Failed to send message {} to {}: ", webSocketMessage, s, e);
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        log.info("handleTransportError, errror {}", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        log.info("afterConnectionClosed, closeStatus {}", closeStatus);
        users.removeSession(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}