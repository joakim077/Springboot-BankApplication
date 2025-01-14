package com.example.bankapp.listener;

import com.example.bankapp.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    @Autowired
    private MetricsService metricsService;

    @Component
    public static class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
        @Autowired
        private MetricsService metricsService;

        @Override
        public void onApplicationEvent(AuthenticationSuccessEvent event) {
            metricsService.incrementSuccessfulLogins();
            metricsService.incrementLoggedInUsers();
        }
    }

    @Component
    public static class LoginFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
        @Autowired
        private MetricsService metricsService;

        @Override
        public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
            metricsService.incrementFailedLogins();
        }
    }
}