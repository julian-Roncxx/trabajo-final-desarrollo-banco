package com.danidevbank.demo.dto.response;

/** Tema de colores predefinido que el usuario puede elegir con un clic. */
public record ThemePresetResponse(String id, String name, String primaryColor, String accentColor) {
}
