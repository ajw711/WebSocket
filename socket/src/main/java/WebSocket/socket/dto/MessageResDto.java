package WebSocket.socket.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageResDto {

    private String roomId;
    private Long senderId;
    private String message;

    public static MessageResDto createMessage(String roomId, Long senderId, String message){
        return new MessageResDto(roomId, senderId, message);
    }
}
