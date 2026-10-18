package com.tikit.auth.service;

import com.tikit.auth.dto.request.AccountLoginRequestDTO;
import com.tikit.auth.dto.request.AccountRegisterRequestDTO;
import com.tikit.auth.entity.Account;
import com.tikit.auth.entity.Role;
import com.tikit.auth.exception.LoginException;
import com.tikit.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String registerUser(AccountRegisterRequestDTO dto) {

        String encodedPw = passwordEncoder.encode(dto.getRawPassword());

        Account account = Account.create(
                dto.getEmail(),
                encodedPw,
                dto.getRealName(),
                Role.USER
        );

        Account saved = accountRepository.save(account);

        log.info("registered user: {}", saved.getAccountId());

        return saved.getRealName();
    }

    @Transactional(readOnly = true)
    public Account login(AccountLoginRequestDTO dto) {

        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new LoginException("Account Not Found"));

        if(!passwordEncoder.matches(dto.getRawPassword(), account.getPassword())) {
            throw new LoginException("Invalid Password");
        }

        log.info("login success: {}", account.getAccountId());

        return account;
    }

}
