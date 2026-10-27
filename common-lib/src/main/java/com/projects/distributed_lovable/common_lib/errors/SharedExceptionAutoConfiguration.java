package com.projects.distributed_lovable.common_lib.errors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SharedExceptionAutoConfiguration {
    @Bean
    public GlobalExceptionHandler globalExceptionHandler()
    {
        return new GlobalExceptionHandler();
    }
}
