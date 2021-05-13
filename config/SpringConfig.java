package ru.stepanov.springproject.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan("ru.stepanov.springproject")
@EnableWebMvc
public class SpringConfig {
}
