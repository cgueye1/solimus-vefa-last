package com.wakana.vefa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.wakana.vefa.services.StorageService;
import com.wakana.vefa.services.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS S3 Configuration for Production Environment
 * This configuration is activated only when:
 * 1. The 'prod' Spring profile is active, OR
 * 2. The property storage.type=s3 is set
 */
@Configuration
public class S3Config {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Config.class);
    
    /**
     * Configure S3 as the StorageService implementation
     * This bean is only created in production environment
     */
    @Bean
    @Profile("prod")
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3", matchIfMissing = false)
    public StorageService s3StorageService() {
        logger.info("Initializing AWS S3 Storage Service for production environment");
        return new S3Service(
                "${storage.bucket}",
                "${storage.region:eu-west-3}"
        );
    }
}
