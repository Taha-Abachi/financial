package com.pars.financial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api.rate.limit")
public class RateLimitProperties {
    private int maxRequests;
    private int windowSeconds;
    private int banPeriodSeconds;

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public int getWindowMilliSeconds() {
        return windowSeconds * 1000;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getBanPeriodSeconds() {
        return banPeriodSeconds;
    }

    public int getBanPeriodMilliSeconds() {
        return banPeriodSeconds * 1000;
    }

    public void setBanPeriodSeconds(int banPeriodSeconds) {
        this.banPeriodSeconds = banPeriodSeconds;
    }
} 