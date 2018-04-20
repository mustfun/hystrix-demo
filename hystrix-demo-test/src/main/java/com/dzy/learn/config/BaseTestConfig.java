
package com.dzy.learn.config;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.dzy.learn"})
public class BaseTestConfig {

    @Configuration
    @PropertySource("classpath:config/application-dev.properties")
    public static class DEV{}
}
