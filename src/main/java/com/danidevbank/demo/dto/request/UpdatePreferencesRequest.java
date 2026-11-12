package com.danidevbank.demo.dto.request;

import com.danidevbank.demo.entity.enums.AppLanguage;
import com.danidevbank.demo.entity.enums.FontSize;
import com.danidevbank.demo.entity.enums.ThemeMode;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** Actualización parcial de las preferencias de interfaz. Los campos null no cambian. */
@Getter
@Setter
public class UpdatePreferencesRequest {

    private ThemeMode themeMode;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color principal debe tener formato #RRGGBB")
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color de acento debe tener formato #RRGGBB")
    private String accentColor;

    /** Atajo: aplica los colores de un tema predefinido (ver GET /api/v1/profile/themes). */
    private String themePreset;

    private FontSize fontSize;

    private AppLanguage language;

    @Pattern(regexp = "^(COP|USD|EUR|MXN|PEN|CLP|ARS|BRL)$", message = "Moneda no soportada")
    private String currency;

    private Boolean hideBalances;
    private Boolean compactMode;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean transactionAlerts;
}
