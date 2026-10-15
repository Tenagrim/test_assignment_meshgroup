package com.tenagrim.meshgrouptestassg.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BalanceScheduler {

    private final AccountService accountService;

    public BalanceScheduler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Scheduled(fixedRate = 5_000)
    public void accrueInterest() {
        System.out.println("SCHEDULER STARTED");
        long startTimeMillis = System.currentTimeMillis();
        accountService.applyInterestAll();
        System.out.println("SCHEDULER FINISHED. time: " + (System.currentTimeMillis() - startTimeMillis) + "ms");

    }
}