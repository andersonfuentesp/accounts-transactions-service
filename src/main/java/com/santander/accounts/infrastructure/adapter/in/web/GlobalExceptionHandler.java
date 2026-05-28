package com.santander.accounts.infrastructure.adapter.in.web;

import com.santander.accounts.application.exception.AuthenticationFailedException;
import com.santander.accounts.application.exception.ConcurrencyConflictException;
import com.santander.accounts.application.exception.IdempotencyConflictException;
import com.santander.accounts.domain.exception.AccountNotFoundException;
import com.santander.accounts.domain.exception.CurrencyMismatchException;
import com.santander.accounts.domain.exception.InsufficientFundsException;
import com.santander.accounts.domain.exception.InvalidAccountDataException;
import com.santander.accounts.domain.exception.UnsupportedCurrencyException;
import com.santander.accounts.infrastructure.security.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - no encontrado
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException ex, WebRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req, "not-found");
    }

    // 422 - reglas de negocio del dominio
    @ExceptionHandler({InsufficientFundsException.class, CurrencyMismatchException.class,
            UnsupportedCurrencyException.class, InvalidAccountDataException.class})
    public ProblemDetail handleBusiness(RuntimeException ex, WebRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio no satisfecha",
                ex.getMessage(), req, "business-rule");
    }

    // 409 - idempotencia / concurrencia
    @ExceptionHandler({IdempotencyConflictException.class, ConcurrencyConflictException.class})
    public ProblemDetail handleConflict(RuntimeException ex, WebRequest req) {
        return build(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), req, "conflict");
    }

    // 401 - autenticación
    @ExceptionHandler(AuthenticationFailedException.class)
    public ProblemDetail handleAuth(AuthenticationFailedException ex, WebRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage(), req, "unauthorized");
    }

    // 400 - validación de cuerpo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Datos inválidos");
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", detail, req, "validation");
    }

    // 400 - header obligatorio ausente (ej. Idempotency-Key)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex, WebRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Cabecera requerida ausente",
                "Falta la cabecera: " + ex.getHeaderName(), req, "missing-header");
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado", req, "internal");
    }

    private ProblemDetail build(HttpStatus status, String title, String detail,
                                WebRequest req, String type) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://errors.santander.com/" + type));
        pd.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        if (req instanceof ServletWebRequest swr) {
            HttpServletRequest http = swr.getRequest();
            pd.setProperty("path", http.getRequestURI());
        }
        return pd;
    }
}