package com.tenagrim.meshgrouptestassg.service.scheduler;

import com.tenagrim.meshgrouptestassg.service.AccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ResetBalanceRunner implements ApplicationRunner {

    private final AccountService accountService;

    public ResetBalanceRunner(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(ApplicationArguments args) {
        accountService.resetBalancesAll();
    }
}