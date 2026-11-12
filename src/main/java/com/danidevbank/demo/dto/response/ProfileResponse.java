package com.danidevbank.demo.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    private String displayName;
    /** Iniciales para el avatar cuando no hay foto, p. ej. "AG". */
    private String initials;
    private String phone;
    private String bio;
    private LocalDate birthDate;
    private String city;
    private String country;

    private boolean hasAvatar;
    /** Ruta para descargar la foto (requiere token). Null si no hay foto. */
    private String avatarUrl;
    private String avatarColor;
    private LocalDateTime avatarUpdatedAt;

    private int accountsCount;
    private LocalDateTime memberSince;
    private LocalDateTime updatedAt;

    private PreferencesResponse preferences;
}
