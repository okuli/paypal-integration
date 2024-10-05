package com.oket.Integration_of_paypal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vipps")
public class VippsConfig {

    private String mode;
    private Client client;
    private Subscription subscription;
    private Merchant merchant;
    private Api api;
    private String successUrl;
    private String cancelUrl;

    @Data
    public static class Client {
        private String id;
        private String secret;
    }

    @Data
    public static class Subscription {
        private String key;
    }

    @Data
    public static class Merchant {
        private String serial;
    }

    @Data
    public static class Api {
        private String url;
    }

//    @Bean
//    public APIContext apiContext() {
//        return new APIContext(client.getId(), client.getSecret(), mode);
//    }
}