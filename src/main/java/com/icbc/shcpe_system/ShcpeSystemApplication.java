package com.icbc.shcpe_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"share.util", "com.icbc.shcpe_system"})
public class ShcpeSystemApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ShcpeSystemApplication.class)
                .web(WebApplicationType.NONE) // 非 Web 应用
                .run(args);
        //SpringApplication.run(ShcpeSystemApplication.class, args);
    }
}
