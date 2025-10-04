package WebSocket.socket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) // 메시지 생성 시간을 자동으로 기록하기 위해 사용
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    private String roomId;

    @CreatedDate // Spring Data JPA Auditing으로 자동 생성 시간 기록
    @Column(updatable = false, nullable = false)
    private LocalDateTime sendTime;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Chat(Member sender, String roomId, String content) {
        this.sender = sender;
        this.roomId = roomId;
        this.content = content;
    }

    public static Chat createChat(Member sender, String roomId, String content){
        return new Chat(sender, roomId, content);
    }
}
