// package com.example.bankapp.controller;

// import com.example.bankapp.model.Account;
// import com.example.bankapp.service.AccountService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.ui.Model;

// import java.math.BigDecimal;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;

// class BankControllerTest {

//     @Mock
//     private AccountService accountService;

//     @Mock
//     private Model model;

//     @Mock
//     private SecurityContext securityContext;

//     @Mock
//     private Authentication authentication;

//     @InjectMocks
//     private BankController bankController;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);

//         // Mock SecurityContextHolder
//         when(securityContext.getAuthentication()).thenReturn(authentication);
//         SecurityContextHolder.setContext(securityContext);
//     }

//     @Test
//     void testRegisterAccount_Success() {
//         String username = "testuser";
//         String password = "password";

//         String result = bankController.registerAccount(username, password, model);

//         assertThat(result).isEqualTo("redirect:/login");
//         verify(accountService, times(1)).registerAccount(username, password);
//     }

//     @Test
//     void testRegisterAccount_Failure() {
//         String username = "testuser";
//         String password = "password";

//         doThrow(new RuntimeException("Username already exists"))
//                 .when(accountService).registerAccount(username, password);

//         String result = bankController.registerAccount(username, password, model);

//         assertThat(result).isEqualTo("register");
//         verify(model, times(1)).addAttribute(eq("error"), eq("Username already exists"));
//     }

//     @Test
//     void testDeposit() {
//         String username = "testuser";
//         Account account = new Account();
//         account.setUsername(username);
//         account.setBalance(BigDecimal.valueOf(100));

//         BigDecimal amount = BigDecimal.valueOf(50);

//         when(authentication.getName()).thenReturn(username);
//         when(accountService.findAccountByUsername(username)).thenReturn(account);

//         String result = bankController.deposit(amount);

//         assertThat(result).isEqualTo("redirect:/dashboard");
//         verify(accountService, times(1)).deposit(account, amount);
//     }
// }
