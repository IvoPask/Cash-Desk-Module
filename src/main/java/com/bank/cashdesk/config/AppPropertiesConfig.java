package com.bank.cashdesk.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppPropertiesConfig {

    @Value("${spring.security.request-header}")
    private String requestHeader;

    @Value("${spring.security.api-key}")
    private String apiKey;

    @Value("${transaction.history.file}")
    private String transactionFile;

    @Value("${balance.history.file}")
    private String balanceFile;

}
