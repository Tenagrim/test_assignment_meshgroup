package com.tenagrim.meshgrouptestassg.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenagrim.meshgrouptestassg.cache.CacheNames;
import com.tenagrim.meshgrouptestassg.cache.LoggingCacheErrorHandler;
import com.tenagrim.meshgrouptestassg.config.properties.UserCacheProperties;
import com.tenagrim.meshgrouptestassg.dto.response.PageResponse;
import com.tenagrim.meshgrouptestassg.dto.response.UserResponse;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          ObjectMapper objectMapper,
                                          UserCacheProperties properties) {
        JavaType userType = objectMapper.constructType(UserResponse.class);
        JavaType userPageType = objectMapper.getTypeFactory()
                .constructParametricType(PageResponse.class, UserResponse.class);

        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.scan(1000)))
                .withCacheConfiguration(CacheNames.USERS,
                        cacheConfiguration(objectMapper, userType, properties.getUsersTtl()))
                .withCacheConfiguration(CacheNames.USER_SEARCH,
                        cacheConfiguration(objectMapper, userPageType, properties.getUserSearchTtl()))
                .disableCreateOnMissingCache()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    private static RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper, JavaType type, Duration ttl) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(type);
        serializer.setObjectMapper(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
