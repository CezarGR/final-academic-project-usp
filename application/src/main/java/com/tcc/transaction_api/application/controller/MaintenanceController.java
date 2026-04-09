package com.tcc.transaction_api.application.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tcc.transaction_api.domain.exception.OperationTimeoutException;

import lombok.RequiredArgsConstructor;

record ApiResponse(String message, String serverVersion, String requestTimeout, String connectionTimeout, long timestamp) {}

/**
 * REST controller for maintenance
 */
@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    @Value("${server.version}")
    private String serverVersion;

    @Value("${spring.mvc.async.request-timeout}")
    private String requestTimeout;

    @Value("${server.tomcat.connection-timeout}")
    private String connectionTimeout;

    /** Duração total simulada (ms). Cenários podem reduzir o limite abaixo deste valor para forçar 408. */
    @Value("${app.maintenance.timeout-test-delay-ms:5000}")
    private long timeoutTestDelayMs;

    /**
     * Se maior que 0, após este tempo decorrido a requisição falha com {@link OperationTimeoutException} (HTTP 408).
     * 0 = desligado (comportamento: aguarda o delay inteiro e responde 200).
     */
    @Value("${app.maintenance.timeout-test-limit-ms:0}")
    private long timeoutTestLimitMs;

    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse> healthCheck() {
        return ResponseEntity.ok(new ApiResponse("API Up", serverVersion, requestTimeout, connectionTimeout, System.currentTimeMillis()));
    }

    @GetMapping("/timeout-test")
    public ResponseEntity<String> timeoutTest() throws Exception {
        Thread.sleep(2500);
        
        return ResponseEntity.ok("ok");
    }
}

