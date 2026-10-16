package com.tenagrim.meshgrouptestassg.service;


import com.tenagrim.meshgrouptestassg.config.properties.BalanceProperties;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.logging.Logger;


@Service
public class AccountService {
    private final BalanceProperties balancaProperties;

    private final AccountRepository accountRepository;

    public AccountService(BalanceProperties balancaProperties, AccountRepository accountRepository) {
        this.balancaProperties = balancaProperties;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void applyInterestAll() {
        accountRepository.applyInterestForAll(balancaProperties.getGrowthRate(), balancaProperties.getMaxMultiplier());
    }

    @Transactional
    public void resetBalancesAll() {
        int updated = accountRepository.initInitialBalance();
        System.out.println("Initialized initial_balance for "+ updated +" accounts");
    }
}