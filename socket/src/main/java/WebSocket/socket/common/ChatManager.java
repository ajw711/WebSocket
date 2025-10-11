package WebSocket.socket.common;

import WebSocket.socket.dto.AckDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "chat:room:" ;

    /**
     * 메시지 저장 (Stream에 push)
     */
    public String chatSave(String roomId, Long chatId, Long senderId, String message) {
        String streamKey = PREFIX + roomId;

        Map<String, String> entry = new HashMap<>();
        entry.put("senderId", String.valueOf(senderId));
        entry.put("message", message);
        entry.put("chatId", String.valueOf(chatId));

        RecordId recordId = redisTemplate.opsForStream().add(streamKey, entry);

        redisTemplate.opsForStream().add(streamKey, entry);

        return recordId.toString();
    }

    /**
     * 컨슈머 그룹 생성 (채팅방당 1번만)
     */
    public void createConsumerGroupIfAbsent(String roomId) {
        String streamKey = PREFIX + roomId;
        String groupName = "room:" + roomId + ":consumers";

        try {
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName);
            log.info("Consumer group created for {}", streamKey);
        } catch (RedisSystemException e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("Consumer group already exists: {}", groupName);
            } else {
                throw e;
            }
        }
    }

    /**
     * 사용자가 채팅방에 입장할 때 컨슈머 등록 + 이전 메시지 읽기
     */
    public List<MapRecord<String, Object, Object>> joinConsumerGroup(String roomId, Long memberId) {
        String streamKey = PREFIX + roomId;
        String groupName = "room:" + roomId + ":consumers";
        String consumerName = "user:" + memberId;

        boolean consumerExists = isConsumerExists(roomId, memberId);

        if(!consumerExists){
            List<MapRecord<String, Object, Object>> newMessages =
                    redisTemplate.opsForStream().read(
                            Consumer.from(groupName, consumerName),
                            StreamReadOptions.empty().count(10),
                            // 'ReadOffset.lastConsumed()'는 내부적으로 Redis 명령어에서 '>'로 변환됩니다.
                            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                    );
            log.info("신규 입장: memberId={} roomId={}", memberId, roomId);
            return Collections.emptyList();
        }

        //  재입장 → ack 안 된 메시지 확인 후 전송
        List<MapRecord<String, Object, Object>> pendingMessages  =
                redisTemplate.opsForStream().read(
                        Consumer.from(groupName, consumerName),
                        StreamReadOptions.empty().count(10),
                        // 수정된 부분: 미확인 메시지를 가져오기 위해 '0-0' 사용
                        StreamOffset.create(streamKey, ReadOffset.from("0-0"))
                );

        log.info("재입장: memberId={} roomId={} | 미확인 메시지={}", memberId, roomId, pendingMessages.size());

        return pendingMessages;
    }

    /**
     * 특정 유저가 해당 채팅방에 이미 등록되어 있는지 확인
     */
    private boolean isConsumerExists(String roomId, Long memberId) {
        String streamKey = PREFIX + roomId;
        String groupName = "room:" + roomId + ":consumers";
        String consumerName = "user:" + memberId;

        try {
            // 그룹 내 컨슈머 목록 조회
            StreamInfo.XInfoConsumers consumers = redisTemplate.opsForStream()
                    .consumers(streamKey, groupName);

            // 해당 유저가 이미 존재하는지 확인
            return consumers.stream()
                    .anyMatch(c -> c.consumerName().equals(consumerName));
        } catch (Exception e) {
            // 그룹이 없을 수도 있음 → 신규 입장으로 처리
            return false;
        }
    }

    /**
     * 메시지 읽음 처리 (ACK)
     */
    public void ackMessage(AckDto dto, Long memberId) {
        String roomId = dto.getRoomId();
        String streamKey = PREFIX + roomId;
        String groupName = "room:" + roomId + ":consumers";

        List<RecordId> recordIdList = dto.getRecordIds().stream()
                .map(RecordId::of)
                .toList();

        redisTemplate.opsForStream().acknowledge(streamKey, groupName, recordIdList.toArray(new RecordId[0]));
        log.info("ACK: room={}", roomId);
    }


}
