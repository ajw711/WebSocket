package WebSocket.socket.common;


import WebSocket.socket.entity.Member;
import WebSocket.socket.enums.RoleType;
import WebSocket.socket.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component // 1. Spring 빈으로 등록
@RequiredArgsConstructor
public class InitManager {

    private final MemberRepository memberRepository;

    // 순환 참조 회피를 위해 필드 주입으로 변경
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {

        if (memberRepository.findByEmail("test@test.com").isEmpty()) {

            Member newMember = Member.createMember(
                    "홍길동",
                    "test@test.com",
                    passwordEncoder.encode("1234"),
                    RoleType.USER
            );

            memberRepository.save(newMember);
            log.info("초기 사용자 'test@test.com'가 성공적으로 생성되었습니다.");
        } else {
            log.info("초기 사용자 'test@test.com'가 이미 존재합니다.");
        }
    }
}