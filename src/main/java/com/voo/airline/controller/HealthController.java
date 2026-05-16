package com.voo.airline.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Status da API e conexão com banco")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Operation(summary = "Health check da API")
    public ResponseEntity<Map<String, Object>> health() {
        String dbStatus;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbStatus = "connected";
        } catch (Exception e) {
            dbStatus = "disconnected — " + e.getMessage();
        }

        return ResponseEntity.ok(Map.of(
            "status",    "ok",
            "service",   "vOO Airline API",
            "version",   "1.0.0",
            "db",        dbStatus,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
