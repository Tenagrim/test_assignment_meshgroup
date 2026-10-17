package com.tenagrim.meshgrouptestassg.service.scheduler;

import com.tenagrim.meshgrouptestassg.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BalanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(BalanceScheduler.class);

    private final AccountService accountService;

    public BalanceScheduler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Scheduled(fixedRate = 30_000)
    public void accrueInterest() {
        long startTimeMillis = System.currentTimeMillis();
        int updated = accountService.applyInterestAll();
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;

        if (updated > 0) {
            log.info("Interest accrued: accounts={} timeMs={}", updated, elapsedMillis);
        } else {
            log.debug("Interest accrual: no accounts below cap, timeMs={}", elapsedMillis);
        }
    }
}
