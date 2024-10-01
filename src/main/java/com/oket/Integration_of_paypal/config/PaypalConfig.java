package com.oket.Integration_of_paypal.config;

import com.paypal.base.rest.APIContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.Data;


@Data
@Configuration
@ConfigurationProperties(prefix = "paypal")
public class PaypalConfig {

    private String clientId;
    private String clientSecret;
    private String mode;
    private String successUrl;
    private String cancelUrl;

    @Bean
    public APIContext apiContext() {
        return new APIContext(clientId, clientSecret, mode);
    }
}

