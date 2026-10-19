package com.tikit.config.security;

import com.tikit.auth.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class CustomUserDetails extends User {

    private final Account account;

    public CustomUserDetails(Account account) {
        super(
                account.getEmail(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleName().name()))
        );
        this.account = account;
    }


}
