package WebSocket.socket.config;

import WebSocket.socket.interceptor.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 구현한 인터셉터 주입
    private final StompHandler stompHandler;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // 구독 prefix: /topic, /queue
        config.enableSimpleBroker("/topic");
        // 발행 prefix: /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // 클라이언트 접속 엔드포인트: ws://localhost:8080/ws-chat
        // WebSocket Handshake 엔드포인트 설정
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트로부터 들어오는 메시지 채널에 인증 인터셉터를 추가
        registration.interceptors(stompHandler);
        registration.taskExecutor().corePoolSize(4).maxPoolSize(8);
    }
}
