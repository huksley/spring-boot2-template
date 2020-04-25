package com.github.huksley.app.chat;

import com.github.huksley.app.chat.msg.ChatNewUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

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
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws IOException {
        log.info("handleMessage = {}", webSocketMessage);
        if (webSocketMessage instanceof TextMessage) {
            String json = ((TextMessage) webSocketMessage).getPayload();
            ChatTransmission t = ChatTransmission.parse(null, json);
            if (t.type == TransmissionType.message) {
                // Multicast to everyone
                users.forEach((s) -> {
                    try {
                        log.info("Sending {} to {}", webSocketMessage, s);
                        s.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.warn("Failed to send message {} to {}: ", webSocketMessage, s, e);
                    }
                });
            } else
            if (t.type == TransmissionType.newUser) {
                ChatNewUser nu = (ChatNewUser) t;
                users.setUserName(webSocketSession, nu.getUserName());

                // Send updated list of users to everyone
                com.github.huksley.app.chat.msg.ChatUsers uu = new com.github.huksley.app.chat.msg.ChatUsers();
                uu.setUsers(users.getUserNames());
                String njson = ChatTransmission.serialize(uu);
                TextMessage n = new TextMessage(njson);
                users.forEach((s) -> {
                    try {
                        log.info("Sending {} to {}", n, s);
                        s.sendMessage(new TextMessage(njson));
                    } catch (IOException e) {
                        log.warn("Failed to send message {} to {}: ", n, s, e);
                    }
                });
            } else
            if (t.type == TransmissionType.getUsers) {
                // Send current list of users to everyone
                com.github.huksley.app.chat.msg.ChatUsers uu = new com.github.huksley.app.chat.msg.ChatUsers();
                uu.setUsers(users.getUserNames());
                String njson = ChatTransmission.serialize(uu);
                TextMessage n = new TextMessage(njson);
                log.info("Sending {} to {}", n, webSocketSession);
                webSocketSession.sendMessage(n);
            } else {
                log.warn("Unhandled message type", t.type);
            }
        } else {
            log.warn("Invalid message class: {}", webSocketMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        log.info("handleTransportError, error {}", throwable);
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