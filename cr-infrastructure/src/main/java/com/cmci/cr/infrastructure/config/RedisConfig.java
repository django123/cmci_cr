package com.cmci.cr.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Redis pour le caching
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * Configuration de la connexion Redis avec Lettuce
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * ObjectMapper pour la sérialisation JSON avec support Java 8 Time
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * RedisTemplate configuré pour utiliser JSON
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Utilisation de StringRedisSerializer pour les clés
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // Utilisation de GenericJackson2JsonRedisSerializer pour les valeurs
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configuration du CacheManager avec différentes stratégies de TTL
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Configuration par défaut : 1 heure de TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Configurations spécifiques par cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Cache utilisateurs : 2 heures (données changeant peu)
        cacheConfigurations.put("utilisateurs",
                defaultConfig.entryTtl(Duration.ofHours(2)));

        // Cache comptes rendus : 30 minutes (données plus volatiles)
        cacheConfigurations.put("comptes-rendus",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Cache commentaires : 15 minutes
        cacheConfigurations.put("commentaires",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Cache statistiques : 5 minutes (calculs fréquents)
        cacheConfigurations.put("statistiques",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Cache référentiels (régions, zones, églises) : 24 heures
        cacheConfigurations.put("referentiels",
                defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
