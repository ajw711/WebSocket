package WebSocket.socket.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageReqDto {

    private String roomId;
    private String message;

}
