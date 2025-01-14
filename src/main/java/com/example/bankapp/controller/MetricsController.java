package com.example.bankapp.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/custom-metrics")
    public Map<String, Object> getCustomMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("Total Users", getMetricValue("bankapp.total_users"));
        metrics.put("Logged-in Users", getGaugeValue("bankapp.logged_in_users"));
        metrics.put("Failed Logins", getMetricValue("bankapp.failed_logins"));
        metrics.put("Successful Logins", getMetricValue("bankapp.successful_logins"));
        metrics.put("Database Queries", getMetricValue("bankapp.database_queries"));
        metrics.put("API Requests", getMetricValue("bankapp.api_requests"));
        metrics.put("Error Rate", getGaugeValue("bankapp.error_rate"));

        return metrics;
    }

    private double getMetricValue(String metricName) {
        return meterRegistry.find(metricName).counter() != null
                ? meterRegistry.find(metricName).counter().count()
                : 0;
    }

    private double getGaugeValue(String metricName) {
        return meterRegistry.find(metricName).gauge() != null
                ? meterRegistry.find(metricName).gauge().value()
                : 0;
    }
}
