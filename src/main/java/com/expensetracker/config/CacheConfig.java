package com.expensetracker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache configuration for application-level caching
 * Uses simple in-memory cache (ConcurrentHashMap)
 * For production, consider using Redis or Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("currencies"),        // Static currency data
                new ConcurrentMapCache("users")              // User authentication details (CRITICAL for performance - caches every authenticated request)
        ));
        return cacheManager;
    }
}
