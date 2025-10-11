package WebSocket.socket.controller;

import WebSocket.socket.common.ChatManager;
import WebSocket.socket.dto.AckDto;
import WebSocket.socket.dto.MessageReqDto;
import WebSocket.socket.dto.MessageResDto;
import WebSocket.socket.dto.ReconnectReqDto;
import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatManager chatManager;

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
        log.info("ackMessage = {} ", dto.getRecordIds());
        Long memberId = Long.parseLong(principal.getName());
        chatService.ackMessage(dto, memberId);
    }

    @SubscribeMapping("/init/room/{roomId}")
    public List<MessageResDto> handleSubscribe(@DestinationVariable String roomId, Principal principal) {
        Long memberId = Long.parseLong(principal.getName());

        // Consumer group 가입 + ack 미확인 메시지 조회
        List<MapRecord<String, Object, Object>> recentMessages = chatManager.joinConsumerGroup(roomId, memberId);
        log.info("handleSubscribe = {} ", roomId);
        // DTO 변환 후 반환
        return recentMessages.stream()
                .map(record -> {
                    Map<Object, Object> value = record.getValue();
                    return MessageResDto.createMessage(
                            roomId,
                            record.getId().toString(),
                            Long.parseLong((String) value.get("senderId")),
                            (String) value.get("message")
                    );
                }).toList();
    }


}
