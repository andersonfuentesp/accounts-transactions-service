package com.santander.accounts.infrastructure.adapter.in.web.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}