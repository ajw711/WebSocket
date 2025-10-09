package WebSocket.socket.service;

import WebSocket.socket.common.SessionManager;
import WebSocket.socket.dto.MessageDto;
import WebSocket.socket.entity.Chat;
import WebSocket.socket.entity.Member;
import WebSocket.socket.repository.ChatRepository;
import WebSocket.socket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    public void saveMessage(Long senderId, MessageDto messageDto) {

        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Chat chat = Chat.createChat(
                sender,
                messageDto.getRoomId(),
                messageDto.getMessage()
        );


        chatRepository.save(chat);

        //  구독 클라이언트에게 메시지 브로드캐스트
        // 전송할 DTO를 새로 생성합니다. (클라이언트가 발신자 ID를 알 수 있도록)
        MessageDto broadcastDto = MessageDto.createMessage(
                messageDto.getRoomId(),
                senderId, // Long 타입의 senderId를 DTO에 포함
                messageDto.getMessage()
        );

        // 브로드캐스트: /topic/room/{roomId} 경로를 구독하는 모든 클라이언트에게 전송
        String destination = "/topic/room/" + messageDto.getRoomId();
        messagingTemplate.convertAndSend(destination, broadcastDto);
    }


    // DisconnectSocket remove
    public void removeUserSession(String sessionId) {
        sessionManager.removeSession(sessionId);
    }
}
