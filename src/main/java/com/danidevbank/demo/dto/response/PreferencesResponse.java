package com.danidevbank.demo.dto.response;

import com.danidevbank.demo.entity.enums.AppLanguage;
import com.danidevbank.demo.entity.enums.FontSize;
import com.danidevbank.demo.entity.enums.ThemeMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreferencesResponse {
    private ThemeMode themeMode;
    private String primaryColor;
    private String accentColor;
    private FontSize fontSize;
    private AppLanguage language;
    private String currency;
    private boolean hideBalances;
    private boolean compactMode;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean transactionAlerts;
}
