package com.tenagrim.meshgrouptestassg.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.cache")
public class UserCacheProperties {
    private Duration usersTtl = Duration.ofMinutes(10);
    private Duration userSearchTtl = Duration.ofMinutes(1);

    public Duration getUsersTtl() {
        return usersTtl;
    }

    public void setUsersTtl(Duration usersTtl) {
        this.usersTtl = usersTtl;
    }

    public Duration getUserSearchTtl() {
        return userSearchTtl;
    }

    public void setUserSearchTtl(Duration userSearchTtl) {
        this.userSearchTtl = userSearchTtl;
    }
}
