package WebSocket.socket.controller;

import WebSocket.socket.dto.MessageDto;
import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
        chatService.saveMessage(Long.parseLong(principal.getName()), message);
    }
}
