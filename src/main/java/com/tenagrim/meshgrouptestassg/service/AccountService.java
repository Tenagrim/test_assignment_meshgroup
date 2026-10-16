package com.tenagrim.meshgrouptestassg.service;


import com.tenagrim.meshgrouptestassg.config.properties.BalanceProperties;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final BalanceProperties balanceProperties;

    private final AccountRepository accountRepository;

    public AccountService(BalanceProperties balanceProperties, AccountRepository accountRepository) {
        this.balanceProperties = balanceProperties;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public int applyInterestAll() {
        return accountRepository.applyInterestForAll(balanceProperties.getGrowthRate(), balanceProperties.getMaxMultiplier());
    }

    @Transactional
    public void resetBalancesAll() {
        int updated = accountRepository.initInitialBalance();
        log.info("Balances reset to initial values: accounts={}", updated);
    }
}
