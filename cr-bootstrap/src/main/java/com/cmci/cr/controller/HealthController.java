package com.cmci.cr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour vérifier que l'API fonctionne
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Endpoints de santé et test de l'API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Vérifier l'état de l'application",
               description = "Retourne l'état de santé de l'application avec la date/heure actuelle")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "CMCI CR Backend");
        response.put("version", "1.0.0-SNAPSHOT");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping simple", description = "Endpoint simple pour vérifier que l'API répond")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
