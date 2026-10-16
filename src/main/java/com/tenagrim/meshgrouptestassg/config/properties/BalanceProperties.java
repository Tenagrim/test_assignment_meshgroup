package com.tenagrim.meshgrouptestassg.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.balance")
public class BalanceProperties {
    private BigDecimal growthRate = new BigDecimal("1.10");
    private BigDecimal maxMultiplier = new BigDecimal("2.07");

    public BigDecimal getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(BigDecimal growthRate) {
        this.growthRate = growthRate;
    }

    public BigDecimal getMaxMultiplier() {
        return maxMultiplier;
    }

    public void setMaxMultiplier(BigDecimal maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }
}
