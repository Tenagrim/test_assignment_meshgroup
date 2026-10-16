package com.tenagrim.meshgrouptestassg.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class LoggingCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache get failed, falling back to DB: cache={} key={} error={}", cache.getName(), key, exception.toString());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Cache put failed: cache={} key={} error={}", cache.getName(), key, exception.toString());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Cache evict failed, entry may stay stale until TTL: cache={} key={} error={}", cache.getName(), key, exception.toString());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Cache clear failed, entries may stay stale until TTL: cache={} error={}", cache.getName(), exception.toString());
    }
}
