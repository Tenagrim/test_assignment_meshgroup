package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.dto.request.TransferRequest;
import com.tenagrim.meshgrouptestassg.dto.response.TransferResponse;
import com.tenagrim.meshgrouptestassg.entity.Account;
import com.tenagrim.meshgrouptestassg.exception.*;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransferService(AccountRepository accountRepository,
                           UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResponse transfer(Long fromUserId, TransferRequest request) {
        Long toUserId = request.getUserId();
        BigDecimal amount = request.getValue();

        if (fromUserId.equals(toUserId)) {
            throw new SelfTransferException();
        }

        if (!userRepository.existsById(toUserId)) {
            throw new UserNotFoundException(toUserId);
        }

        // deadlock protection
        Long firstId = Math.min(fromUserId, toUserId);
        Long secondId = Math.max(fromUserId, toUserId);

        Account first = accountRepository.findByUserIdForUpdate(firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));
        Account second = accountRepository.findByUserIdForUpdate(secondId)
                .orElseThrow(() -> new AccountNotFoundException(secondId));

        Account from = first.getUser().getId().equals(fromUserId) ? first : second;
        Account to = first.getUser().getId().equals(fromUserId) ? second : first;

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromUserId, amount, from.getBalance());
        }

        BigDecimal fromBefore = from.getBalance();
        BigDecimal toBefore = to.getBalance();

        from.setBalance(fromBefore.subtract(amount));
        to.setBalance(toBefore.add(amount));

        log.info("Transfer: from={} to={} amount={} fromBalance: {} -> {} toBalance: {} -> {}",
                fromUserId, toUserId, amount,
                fromBefore, from.getBalance(),
                toBefore, to.getBalance());

        return new TransferResponse(
                fromUserId,
                toUserId,
                amount,
                from.getBalance(),
                to.getBalance()
        );
    }
}
