package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterAccount_Success() {
        String username = "testuser";
        String password = "password";

        when(accountRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        Account savedAccount = new Account();
        savedAccount.setUsername(username);
        savedAccount.setPassword("encodedPassword");
        savedAccount.setBalance(BigDecimal.ZERO);

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.registerAccount(username, password);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testRegisterAccount_UsernameExists() {
        String username = "testuser";
        String password = "password";

        when(accountRepository.findByUsername(username)).thenReturn(Optional.of(new Account()));

        assertThrows(RuntimeException.class, () -> accountService.registerAccount(username, password));
    }

    @Test
    void testDeposit() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);

        accountService.deposit(account, amount);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_Success() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(100));

        BigDecimal amount = BigDecimal.valueOf(50);

        accountService.withdraw(account, amount);

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50));
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientFunds() {
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(50));

        BigDecimal amount = BigDecimal.valueOf(100);

        assertThrows(RuntimeException.class, () -> accountService.withdraw(account, amount));
    }

    @Test
    void testTransferAmount_Success() {
        Account fromAccount = new Account();
        fromAccount.setBalance(BigDecimal.valueOf(100));
        fromAccount.setUsername("sender");

        Account toAccount = new Account();
        toAccount.setBalance(BigDecimal.valueOf(50));
        toAccount.setUsername("receiver");

        when(accountRepository.findByUsername("receiver")).thenReturn(Optional.of(toAccount));

        accountService.transferAmount(fromAccount, "receiver", BigDecimal.valueOf(50));

        assertThat(fromAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));

        verify(accountRepository, times(1)).save(fromAccount);
        verify(accountRepository, times(1)).save(toAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}