package com.danidevbank.demo.entity;

import com.danidevbank.demo.entity.enums.AppLanguage;
import com.danidevbank.demo.entity.enums.FontSize;
import com.danidevbank.demo.entity.enums.ThemeMode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Preferencias de interfaz del usuario: colores, modo claro/oscuro, idioma,
 * tamaño de letra, privacidad y notificaciones.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    public static final String DEFAULT_PRIMARY_COLOR = "#2563EB";
    public static final String DEFAULT_ACCENT_COLOR = "#10B981";

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private ThemeMode themeMode = ThemeMode.SYSTEM;

    /** Color principal de la interfaz en formato hexadecimal (#RRGGBB). */
    @Column(length = 7)
    @Builder.Default
    private String primaryColor = DEFAULT_PRIMARY_COLOR;

    /** Color secundario / de acento en formato hexadecimal (#RRGGBB). */
    @Column(length = 7)
    @Builder.Default
    private String accentColor = DEFAULT_ACCENT_COLOR;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private FontSize fontSize = FontSize.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    @Builder.Default
    private AppLanguage language = AppLanguage.ES;

    /** Moneda en la que se muestran los montos (ISO 4217). */
    @Column(length = 3)
    @Builder.Default
    private String currency = "COP";

    /** Oculta los saldos en pantalla hasta que el usuario los revele. */
    @Builder.Default
    private Boolean hideBalances = false;

    /** Interfaz más densa, con menos espaciado. */
    @Builder.Default
    private Boolean compactMode = false;

    @Builder.Default
    private Boolean emailNotifications = true;

    @Builder.Default
    private Boolean pushNotifications = true;

    /** Avisar por cada transferencia enviada o recibida. */
    @Builder.Default
    private Boolean transactionAlerts = true;

    public static UserPreferences defaults() {
        return UserPreferences.builder().build();
    }
}
