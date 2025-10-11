package WebSocket.socket.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "chat:room:" ;

    public void chatSave(String roomId, Long chatId, Long senderId, String message) {

        String streamKey = PREFIX + roomId;

        Map<String, String> entry = new HashMap<>();
        entry.put("senderId", String.valueOf(senderId));
        entry.put("message", message);
        entry.put("chatId", String.valueOf(chatId));

        redisTemplate.opsForStream().add(streamKey, entry);
    }

    public List<MapRecord<String, Object, Object>> getRecentMessage(String roomId, int count){
        String streamKey = PREFIX + roomId;

        return redisTemplate.opsForStream()
                .reverseRange(
                streamKey,
                Range.unbounded(),
                Limit.limit().count(count)
        );
    }

    public void ackMessage(String roomId, String recordId){

        String streamKey = "chat:room:" + roomId;
        String group = "room:" + roomId + ":consumers";

        redisTemplate.opsForStream()
                .acknowledge(streamKey, group, recordId);
    }

    public void createConsumerGroupIfAbsent(String roomId) {
        String streamKey = PREFIX + roomId;
        String group = "room:" + roomId + ":consumers";

        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), group);
        } catch (RedisSystemException e) {
            if (e.getMessage().contains("BUSYGROUP")) {
                // 이미 그룹이 존재하면 무시
            } else {
                throw e;
            }
        }
    }


}
