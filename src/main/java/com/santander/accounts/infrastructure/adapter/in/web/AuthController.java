package com.santander.accounts.infrastructure.adapter.in.web;

import com.santander.accounts.application.port.in.AuthenticateUseCase;
import com.santander.accounts.application.port.in.AuthenticateUseCase.LoginCommand;
import com.santander.accounts.infrastructure.adapter.in.web.dto.LoginRequest;
import com.santander.accounts.infrastructure.adapter.in.web.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;

    public AuthController(AuthenticateUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica al usuario y devuelve un JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authenticateUseCase.login(new LoginCommand(request.username(), request.password()));
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", 3600));
    }
}