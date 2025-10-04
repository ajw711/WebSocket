package WebSocket.socket.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageDto {

    private String roomId;
    private Long senderId;
    private String message;

    public static MessageDto createMessage(String roomId, Long senderId, String message){
        return new MessageDto(roomId, senderId, message);
    }
}
