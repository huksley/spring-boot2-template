package com.github.huksley.app.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class ChatUsers {

    Map<String, WebSocketSession> userSessions;

    public void lazyInit() {
        if (userSessions == null) {
            userSessions = new ConcurrentHashMap<>();
        }
    }

    public void addSession(WebSocketSession s) {
        lazyInit();
        userSessions.put(s.getId(), s);
    }

    public void removeSession(WebSocketSession s) {
        userSessions.remove(s.getId());
    }

    public void forEach(Consumer<WebSocketSession> f) {
        userSessions.forEach((s, webSocketSession) -> f.accept(webSocketSession));
    }
}
