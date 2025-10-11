package WebSocket.socket.controller;

import WebSocket.socket.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public void createRoom(@RequestParam String roomId) {
        chatService.createConsumerGroupIfAbsent(roomId);
    }
}
