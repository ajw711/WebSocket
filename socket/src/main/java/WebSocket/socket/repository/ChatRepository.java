package WebSocket.socket.repository;

import WebSocket.socket.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // lastMessageId 이후의 메시지 목록 가져오기
    List<Chat> findByRoomIdAndIdGreaterThanOrderByIdAsc(String roomId, Long lastMessageId);
}
