package com.vault.websocket;

import com.vault.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsTokenInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null) {
            log.warn("WebSocket connection rejected: no query params");
            return false;
        }

        String token = UriComponentsBuilder.newInstance()
                .query(query)
                .build()
                .getQueryParams()
                .getFirst("TOKEN");

        if (token == null || !jwtProvider.validateToken(token)) {
            log.warn("WebSocket connection rejected: invalid TOKEN");
            return false;
        }

        attributes.put("sub", jwtProvider.getSubject(token));
        attributes.put("email", jwtProvider.getEmail(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
