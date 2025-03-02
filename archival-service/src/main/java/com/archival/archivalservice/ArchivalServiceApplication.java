package com.archival.archivalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.archival.archivalservice")
@EnableDiscoveryClient
public class ArchivalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchivalServiceApplication.class, args);
    }

}
