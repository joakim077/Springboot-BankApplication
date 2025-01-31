package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.service.AccountService;
import com.example.bankapp.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class BankController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        model.addAttribute("account", account);
        return "dashboard";
    }

    @PostMapping("/logout")
    public String logoutUser() {
        metricsService.decrementLoggedInUsers(); // Decrement logged-in users on logout
        return "redirect:/login?logout";
    }


    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // Return the register page
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            accountService.registerAccount(username, password);
            metricsService.incrementTotalUsers(); // Increment total users
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
     
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Return the login page
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            if (accountService.validateCredentials(username, password)) {
                metricsService.incrementSuccessfulLogins(); // Increment successful logins
                return "redirect:/dashboard";
            } else {
                metricsService.incrementFailedLogins(); // Increment failed logins
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception e) {
            metricsService.incrementErrorRate(); // Increment error rate for login issues
            model.addAttribute("error", "An error occurred");
            return "login";
        }
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        accountService.deposit(account, amount);

        metricsService.incrementApiRequests(); // Increment API request count
        metricsService.incrementDatabaseQueries(); // Increment database query count
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        try {
            accountService.withdraw(account, amount);
        } catch (RuntimeException e) {
            metricsService.incrementErrorRate(); // Increment error rate for failed withdrawal
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", account);
            return "dashboard";
        }

        metricsService.incrementApiRequests(); // Increment API request count
        metricsService.incrementDatabaseQueries(); // Increment database query count
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions")
    public String transactionHistory(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        model.addAttribute("transactions", accountService.getTransactionHistory(account));
        return "transactions";
    }

    @PostMapping("/transfer")
    public String transferAmount(@RequestParam String toUsername, @RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount = accountService.findAccountByUsername(username);

        try {
            accountService.transferAmount(fromAccount, toUsername, amount);
        } catch (RuntimeException e) {
            metricsService.incrementErrorRate(); // Increment error rate for failed transfer
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", fromAccount);
            return "dashboard";
        }

        metricsService.incrementApiRequests(); // Increment API request count
        metricsService.incrementDatabaseQueries(); // Increment database query count
        return "redirect:/dashboard";
    }
}
