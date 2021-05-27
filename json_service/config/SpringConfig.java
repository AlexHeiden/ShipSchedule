package ru.stepanov.json_service.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan("ru.stepanov.json_service")
@EnableWebMvc
public class SpringConfig {
}
