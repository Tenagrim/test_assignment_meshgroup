package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.dto.request.TransferRequest;
import com.tenagrim.meshgrouptestassg.dto.response.TransferResponse;
import com.tenagrim.meshgrouptestassg.entity.Account;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.exception.AccountNotFoundException;
import com.tenagrim.meshgrouptestassg.exception.InsufficientFundsException;
import com.tenagrim.meshgrouptestassg.exception.InvalidTransferException;
import com.tenagrim.meshgrouptestassg.exception.SelfTransferException;
import com.tenagrim.meshgrouptestassg.exception.UserNotFoundException;
import com.tenagrim.meshgrouptestassg.repository.AccountRepository;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Long FROM_ID = 1L;
    private static final Long TO_ID = 2L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transfer_movesMoneyBetweenAccounts() {
        Account from = account(FROM_ID, "100.00");
        Account to = account(TO_ID, "50.00");
        mockAccounts(from, to);

        TransferResponse response = transferService.transfer(FROM_ID, request(TO_ID, "30.25"));

        assertThat(from.getBalance()).isEqualByComparingTo("69.75");
        assertThat(to.getBalance()).isEqualByComparingTo("80.25");
        assertThat(response.getFromUserId()).isEqualTo(FROM_ID);
        assertThat(response.getToUserId()).isEqualTo(TO_ID);
        assertThat(response.getAmount()).isEqualByComparingTo("30.25");
        assertThat(response.getFromBalanceAfter()).isEqualByComparingTo("69.75");
        assertThat(response.getToBalanceAfter()).isEqualByComparingTo("80.25");
    }

    @Test
    void transfer_allowsWholeBalance() {
        Account from = account(FROM_ID, "100.00");
        Account to = account(TO_ID, "0.00");
        mockAccounts(from, to);

        transferService.transfer(FROM_ID, request(TO_ID, "100.00"));

        assertThat(from.getBalance()).isEqualByComparingTo("0.00");
        assertThat(to.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void transfer_locksAccountsInAscendingUserIdOrder() {
        Account from = account(5L, "100.00");
        Account to = account(3L, "0.00");
        when(userRepository.existsById(3L)).thenReturn(true);
        when(accountRepository.findByUserIdForUpdate(3L)).thenReturn(Optional.of(to));
        when(accountRepository.findByUserIdForUpdate(5L)).thenReturn(Optional.of(from));

        transferService.transfer(5L, request(3L, "10.00"));

        InOrder order = inOrder(accountRepository);
        order.verify(accountRepository).findByUserIdForUpdate(3L);
        order.verify(accountRepository).findByUserIdForUpdate(5L);
        assertThat(from.getBalance()).isEqualByComparingTo("90.00");
        assertThat(to.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void transfer_insufficientFunds_keepsBalances() {
        Account from = account(FROM_ID, "10.00");
        Account to = account(TO_ID, "50.00");
        mockAccounts(from, to);

        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(TO_ID, "10.01")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(from.getBalance()).isEqualByComparingTo("10.00");
        assertThat(to.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void transfer_toSelf_isRejected() {
        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(FROM_ID, "10.00")))
                .isInstanceOf(SelfTransferException.class);

        verify(accountRepository, never()).findByUserIdForUpdate(any());
    }

    @Test
    void transfer_unknownRecipient_isRejected() {
        when(userRepository.existsById(TO_ID)).thenReturn(false);

        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(TO_ID, "10.00")))
                .isInstanceOf(UserNotFoundException.class);

        verify(accountRepository, never()).findByUserIdForUpdate(any());
    }

    @Test
    void transfer_missingAccount_isRejected() {
        when(userRepository.existsById(TO_ID)).thenReturn(true);
        when(accountRepository.findByUserIdForUpdate(FROM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(TO_ID, "10.00")))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-100", "0.001", "10.005"})
    void transfer_invalidAmount_isRejected(String amount) {
        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(TO_ID, amount)))
                .isInstanceOf(InvalidTransferException.class);

        verify(accountRepository, never()).findByUserIdForUpdate(any());
    }

    @Test
    void transfer_amountWithTrailingZeros_isAccepted() {
        Account from = account(FROM_ID, "100.00");
        Account to = account(TO_ID, "0.00");
        mockAccounts(from, to);

        transferService.transfer(FROM_ID, request(TO_ID, "10.5000"));

        assertThat(from.getBalance()).isEqualByComparingTo("89.50");
    }

    @Test
    void transfer_nullAmount_isRejected() {
        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(TO_ID, null)))
                .isInstanceOf(InvalidTransferException.class);
    }

    @Test
    void transfer_nullRecipient_isRejected() {
        assertThatThrownBy(() -> transferService.transfer(FROM_ID, request(null, "10.00")))
                .isInstanceOf(InvalidTransferException.class);
    }

    private void mockAccounts(Account from, Account to) {
        when(userRepository.existsById(to.getUser().getId())).thenReturn(true);
        when(accountRepository.findByUserIdForUpdate(from.getUser().getId())).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(to.getUser().getId())).thenReturn(Optional.of(to));
    }

    private static Account account(Long userId, String balance) {
        User user = new User();
        user.setId(userId);
        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal(balance));
        account.setInitialBalance(new BigDecimal(balance));
        return account;
    }

    private static TransferRequest request(Long toUserId, String amount) {
        TransferRequest request = new TransferRequest();
        request.setUserId(toUserId);
        request.setValue(amount == null ? null : new BigDecimal(amount));
        return request;
    }
}
