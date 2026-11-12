package com.danidevbank.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos personalizables del perfil. Se guarda aparte de {@link User} para no
 * mezclar credenciales con datos de presentación (foto, biografía, tema...).
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Nombre o apodo que se muestra en la interfaz. */
    @Column(length = 50)
    private String displayName;

    @Column(length = 20)
    private String phone;

    @Column(length = 250)
    private String bio;

    private LocalDate birthDate;

    @Column(length = 60)
    private String city;

    @Column(length = 60)
    private String country;

    /** Foto de perfil (bytes de la imagen). */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] avatar;

    @Column(length = 30)
    private String avatarContentType;

    private LocalDateTime avatarUpdatedAt;

    /** Color de fondo del avatar con iniciales cuando no hay foto. */
    @Column(length = 7)
    private String avatarColor;

    @Embedded
    @Builder.Default
    private UserPreferences preferences = UserPreferences.defaults();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean hasAvatar() {
        return avatar != null && avatar.length > 0;
    }
}
