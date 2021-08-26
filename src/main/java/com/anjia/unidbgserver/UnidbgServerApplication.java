package com.anjia.unidbgserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(proxyTargetClass = true)
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"com.anjia"})
public class UnidbgServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnidbgServerApplication.class, args);
    }

}
