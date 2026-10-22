package com.tenagrim.meshgrouptestassg.service;


import com.tenagrim.meshgrouptestassg.config.properties.BalanceProperties;
import com.tenagrim.meshgrouptestassg.exception.InsufficientFundsException;
import com.tenagrim.meshgrouptestassg.exception.PhoneAlreadyTakenException;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.tenagrim.meshgrouptestassg.util.Persistence.isConstraint;


@Service
public class AccountService {

    private static final String BALANCE_NON_NEGATIVE_CONSTRAINT = "chk_account_balance_non_negative";
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final BalanceProperties balanceProperties;

    private final AccountRepository accountRepository;
    private final OutboxService outboxService;

    public AccountService(BalanceProperties balanceProperties, AccountRepository accountRepository, OutboxService outboxService) {
        this.balanceProperties = balanceProperties;
        this.accountRepository = accountRepository;
        this.outboxService = outboxService;
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

    @Transactional
    public void changeBalance(Long userId, BigDecimal amount){
        try{
            accountRepository.changeBalanceAndSaveOutbox(userId, amount, "{\"text\":\"balance changed\"}");
        } catch (DataIntegrityViolationException e) {
            if (isConstraint(e, BALANCE_NON_NEGATIVE_CONSTRAINT))
                throw new InsufficientFundsException(userId, amount);
            else throw e;
        }
    }
}
