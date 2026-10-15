package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.entity.Account;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AccountService {
    private static final BigDecimal GROWTH_RATE = new BigDecimal("1.10");
    private static final BigDecimal MAX_MULTIPLIER = new BigDecimal("2.07");

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void applyInterestAll() {
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            applyInterest(account);
        }
    }

    private void applyInterest(Account account) {
        BigDecimal current = account.getBalance();
        BigDecimal max = account.getInitialBalance()
                .multiply(MAX_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);

        if (current.compareTo(max) >= 0) {
            return;
        }

        BigDecimal increased = current
                .multiply(GROWTH_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        if (increased.compareTo(max) > 0) {
            increased = max;
        }

        account.setBalance(increased);
    }
}