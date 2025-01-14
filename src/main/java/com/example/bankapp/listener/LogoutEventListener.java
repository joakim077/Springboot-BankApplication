package com.example.bankapp.listener;

import com.example.bankapp.service.MetricsService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LogoutEventListener implements ApplicationListener<LogoutSuccessEvent> {

    private final MetricsService metricsService;

    public LogoutEventListener(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
        metricsService.decrementLoggedInUsers();
    }
}