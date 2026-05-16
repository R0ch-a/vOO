package com.voo.airline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita o suporte a métodos assíncronos ({@code @Async})
 * para que os Listeners do padrão Observer não bloqueiem a thread HTTP.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
