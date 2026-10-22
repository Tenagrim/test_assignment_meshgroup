package com.tenagrim.meshgrouptestassg.dto.response;

import java.math.BigDecimal;

public class TransferResponse {

    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private BigDecimal fromBalanceAfter;
    private BigDecimal toBalanceAfter;

    public TransferResponse(Long fromUserId, Long toUserId, BigDecimal amount,
                            BigDecimal fromBalanceAfter, BigDecimal toBalanceAfter) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.fromBalanceAfter = fromBalanceAfter;
        this.toBalanceAfter = toBalanceAfter;
    }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFromBalanceAfter() { return fromBalanceAfter; }
    public void setFromBalanceAfter(BigDecimal fromBalanceAfter) { this.fromBalanceAfter = fromBalanceAfter; }

    public BigDecimal getToBalanceAfter() { return toBalanceAfter; }
    public void setToBalanceAfter(BigDecimal toBalanceAfter) { this.toBalanceAfter = toBalanceAfter; }
}
