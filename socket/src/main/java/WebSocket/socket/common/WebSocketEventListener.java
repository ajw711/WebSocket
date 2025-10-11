package WebSocket.socket.common;

import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {


    private final ChatService chatService;

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();

        String memberId = principal.getName();

        log.info("WebSocket 종료 감지, sessionId={}, user={}", sessionId, principal.getName());

        chatService.markOffline(memberId, sessionId);
    }
}
