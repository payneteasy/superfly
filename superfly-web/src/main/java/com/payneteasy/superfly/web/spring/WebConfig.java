package com.payneteasy.superfly.web.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan("com.payneteasy.superfly.web.controller.api")
public class WebConfig {

}
