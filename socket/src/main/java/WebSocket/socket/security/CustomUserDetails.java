package WebSocket.socket.security;

import WebSocket.socket.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole()));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getId().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료되지 않음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠기지 않음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 만료되지 않음
    }

    @Override
    public boolean isEnabled() {
        return true; // 활성화됨
    }
}