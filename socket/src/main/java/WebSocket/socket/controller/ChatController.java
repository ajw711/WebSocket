package WebSocket.socket.controller;

import WebSocket.socket.dto.MessageDto;
import WebSocket.socket.security.CustomUserDetails;
import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/room")
    public void sendMessage(
            MessageDto message,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        log.info("sendMessage = {} ", message.getMessage());
        chatService.saveMessage(Long.parseLong(customUserDetails.getUsername()), message);
    }
}
