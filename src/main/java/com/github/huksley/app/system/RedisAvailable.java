package com.github.huksley.app.system;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisAvailable implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        String cacheType = env.resolvePlaceholders("${spring.cache.type:NONE}");
        return "redis".equalsIgnoreCase(cacheType);
    }
}
