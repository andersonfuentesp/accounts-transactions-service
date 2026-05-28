package com.santander.accounts.application.port.in;

import java.util.List;

/** Resultado paginado genérico. */
public record PageResult<T>(List<T> items, int offset, int limit, long total) {}