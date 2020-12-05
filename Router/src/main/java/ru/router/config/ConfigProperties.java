package ru.router.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ConfigProperties {

    @Value("${market.port}")
    private int marketPort;
    @Value("${market.address}")
    private String marketAddress;
    @Value("${broker.port}")
    private int brokerPort;
    @Value("${broker.address}")
    private String brokerAddress;
}
