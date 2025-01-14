package com.example.bankapp.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final Counter totalUsersCounter;
    private final Counter failedLoginCounter;
    private final Counter successfulLoginCounter;
    private final Counter databaseQueryCounter;
    private final Counter apiRequestCounter;
    private final AtomicInteger loggedInUsersGauge;
    private final AtomicInteger errorRateGauge;

    public MetricsService(MeterRegistry meterRegistry) {
        // Counters
        totalUsersCounter = meterRegistry.counter("bankapp.total_users");
        failedLoginCounter = meterRegistry.counter("bankapp.failed_logins");
        successfulLoginCounter = meterRegistry.counter("bankapp.successful_logins");
        databaseQueryCounter = meterRegistry.counter("bankapp.database_queries");
        apiRequestCounter = meterRegistry.counter("bankapp.api_requests");

        // Gauges for tracking real-time values
        loggedInUsersGauge = meterRegistry.gauge("bankapp.logged_in_users", new AtomicInteger(0));
        errorRateGauge = meterRegistry.gauge("bankapp.error_rate", new AtomicInteger(0));
    }

    // Increment total users count
    public void incrementTotalUsers() {
        totalUsersCounter.increment();
    }

    // Increment logged-in users count
    public void incrementLoggedInUsers() {
        loggedInUsersGauge.incrementAndGet();
    }

    // Decrement logged-in users count
    public void decrementLoggedInUsers() {
        loggedInUsersGauge.decrementAndGet();
    }

    // Increment failed logins count
    public void incrementFailedLogins() {
        failedLoginCounter.increment();
    }

    // Increment successful logins count
    public void incrementSuccessfulLogins() {
        successfulLoginCounter.increment();
    }

    // Increment database queries count
    public void incrementDatabaseQueries() {
        databaseQueryCounter.increment();
    }

    // Increment API requests count
    public void incrementApiRequests() {
        apiRequestCounter.increment();
    }

    // Track error rate
    public void incrementErrorRate() {
        errorRateGauge.incrementAndGet();
    }

    public void decrementErrorRate() {
        errorRateGauge.decrementAndGet();
    }
}