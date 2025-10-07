package WebSocket.socket.controller;

import WebSocket.socket.dto.MessageDto;
import WebSocket.socket.security.CustomUserDetails;
import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            MessageDto message,
            SimpMessageHeaderAccessor headerAccessor){
        log.info("sendMessage = {} ", message.getMessage());
        // Principal을 통해 인증 정보에 접근
        Principal principal = headerAccessor.getUser();
        if (principal != null) {
            String memberId = principal.getName();
            // ... 메시지 처리 로직
        } else {
            // 이 로그는 더 이상 발생하지 않습니다.
            log.warn("인증 정보가 없습니다.");
        }
        chatService.saveMessage(Long.parseLong(principal.getName()), message);
    }
}
