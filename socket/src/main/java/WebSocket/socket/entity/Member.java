package WebSocket.socket.entity;

import WebSocket.socket.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.logging.Level;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
public class Member extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;

    private String email;

    private String password;

    private RoleType role;

    public static Member createMember(String username, String email, String password, RoleType role){
        return Member.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

}
