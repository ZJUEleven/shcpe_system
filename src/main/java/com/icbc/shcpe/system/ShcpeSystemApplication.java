package com.icbc.shcpe.system;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"share.util", "com.icbc.shcpe_system"})
public class ShcpeSystemApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ShcpeSystemApplication.class)
                .web(WebApplicationType.NONE) // 非 Web 应用
                .run(args);
        //SpringApplication.run(ShcpeSystemApplication.class, args);
    }
}
