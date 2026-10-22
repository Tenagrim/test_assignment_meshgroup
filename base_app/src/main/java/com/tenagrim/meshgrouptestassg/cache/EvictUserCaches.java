package com.tenagrim.meshgrouptestassg.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.USERS, key = "#userId"),
        @CacheEvict(cacheNames = CacheNames.USER_SEARCH, allEntries = true)
})
public @interface EvictUserCaches {
}
