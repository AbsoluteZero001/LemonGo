package com.lemongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@MapperScan({"com.lemongo.mapper", "com.lemongo.observability.mapper"})
public class LemonGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemonGoApplication.class, args);
    }
}
