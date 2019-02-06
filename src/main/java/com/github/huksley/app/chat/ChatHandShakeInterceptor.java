package com.github.huksley.app.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

public class ChatHandShakeInterceptor implements HandshakeInterceptor {
    Logger log = LoggerFactory.getLogger(getClass());

    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("After handshake, exception = {}", exception, exception); // log4j two times, last one is for exception stack trace
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
            HttpSession session = servletRequest.getServletRequest().getSession(true);
            Long uid = (Long) session.getAttribute("uid");
            log.info("Before handshake, uid = {}, sessionId = {}", uid, session.getId());
            if (uid != null) {
                map.put("uid", uid);
            }
        }
        return true;
    }
}
