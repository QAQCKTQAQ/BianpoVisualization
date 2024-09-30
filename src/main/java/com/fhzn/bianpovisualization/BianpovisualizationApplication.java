package com.fhzn.bianpovisualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class BianpovisualizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BianpovisualizationApplication.class, args);
    }

}
