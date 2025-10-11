package WebSocket.socket.service;

import WebSocket.socket.common.ChatManager;
import WebSocket.socket.common.SessionManager;
import WebSocket.socket.dto.*;
import WebSocket.socket.entity.Chat;
import WebSocket.socket.entity.Member;
import WebSocket.socket.repository.ChatRepository;
import WebSocket.socket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;
    private final ChatManager chatManager;

    public void saveMessage(Long senderId, MessageReqDto messageDto) {

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Chat chat = Chat.createChat(
                sender,
                messageDto.getRoomId(),
                messageDto.getMessage()
        );


        chatRepository.save(chat);
        String recordId = chatManager.chatSave(messageDto.getRoomId(), chat.getId(), senderId, messageDto.getMessage());
        chatManager.chatSave(messageDto.getRoomId(), chat.getId(), senderId, messageDto.getMessage());

        //  구독 클라이언트에게 메시지 브로드캐스트
        // 전송할 DTO를 새로 생성합니다. (클라이언트가 발신자 ID를 알 수 있도록)
        MessageResDto broadcastDto = MessageResDto.createMessage(
                messageDto.getRoomId(),
                recordId,
                senderId, // Long 타입의 senderId를 DTO에 포함
                messageDto.getMessage()
        );

        // 브로드캐스트: /topic/room/{roomId} 경로를 구독하는 모든 클라이언트에게 전송
        String destination = "/topic/room/" + messageDto.getRoomId();
        messagingTemplate.convertAndSend(destination, broadcastDto);
    }

//    public void handleSubscribe(Long memberId, String roomId) {
//
//
//        chatManager.joinConsumerGroup(roomId, memberId);
//
//
//        List<MapRecord<String, Object, Object>> recentMessages = chatManager.joinConsumerGroup(roomId, memberId);
//
//        List<MessageResDto> messageDtos = recentMessages.stream()
//                .map(record -> {
//                    Map<Object, Object> value = record.getValue();
//                    return MessageResDto.createMessage(
//                            roomId,
//                            Long.parseLong((String) value.get("senderId")),
//                            (String) value.get("message")
//                    );
//                }).toList();
//
//        ReconnectResDto response = ReconnectResDto.of(messageDtos);
//
//        // 클라이언트 개인에게만 전달 (broadcast 아님)
//        String userDestination = "/queue/reconnect/" + memberId;
//        messagingTemplate.convertAndSendToUser(String.valueOf(memberId), userDestination, response );
//    }

    // DisconnectSocket remove
    public void deleteSession(String memberId, String sessionId) {
        sessionManager.deleteSession(sessionId);
    }

    public void ackMessage(AckDto dto, Long memberId) {
        chatManager.ackMessage(dto, memberId);
    }

    public void createConsumerGroupIfAbsent(String roomId) {
        chatManager.createConsumerGroupIfAbsent(roomId);
    }
}
