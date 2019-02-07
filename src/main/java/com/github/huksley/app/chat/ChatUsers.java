package com.github.huksley.app.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void setUserName(WebSocketSession s, String userName) {
        s.getAttributes().put("userName", userName);
    }

    public String[] getUserNames() {
        List<String> l = userSessions.values().stream().filter(s -> s.getAttributes().get("userName") != null).map(s -> (String) s.getAttributes().get("userName")).collect(Collectors.toList());
        return l.toArray(new String[l.size()]);
    }
}
