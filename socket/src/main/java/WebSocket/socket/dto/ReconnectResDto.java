package WebSocket.socket.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReconnectResDto {

    private List<MessageResDto> missedMessage;
    public static ReconnectResDto of(List<MessageResDto> missedMessage){
        return new ReconnectResDto(missedMessage);
    }
}
