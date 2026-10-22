package com.tikit.config.security;

import com.tikit.auth.entity.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class CustomUserDetails extends User {

    private final Member member;

    public CustomUserDetails(Member member) {
        super(
                member.getEmail(),
                member.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()))
        );
        this.member = member;
    }


}
