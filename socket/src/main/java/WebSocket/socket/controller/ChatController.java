package WebSocket.socket.controller;

import WebSocket.socket.dto.AckDto;
import WebSocket.socket.dto.MessageReqDto;
import WebSocket.socket.dto.MessageResDto;
import WebSocket.socket.dto.ReconnectReqDto;
import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            MessageReqDto message,
            SimpMessageHeaderAccessor headerAccessor){
        log.info("sendMessage = {} ", message.getMessage());
        // Principal을 통해 인증 정보에 접근
        Principal principal = headerAccessor.getUser();
        chatService.saveMessage(Long.parseLong(principal.getName()), message);
    }

    @MessageMapping("/chat.ack")
    public void ackMessage(AckDto dto, Principal principal){
        Long memberId = Long.parseLong(principal.getName());
        chatService.ackMessage(dto, principal);
    }

    @SubscribeMapping("/topic/room/{roomId}")
    public void handleSubscribe(@DestinationVariable String roomId,  Principal principal){
        Long memberId = Long.parseLong(principal.getName());
        chatService.getMissedMessages(memberId, roomId);
    }

}
