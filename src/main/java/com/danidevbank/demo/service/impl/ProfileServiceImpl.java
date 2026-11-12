package com.danidevbank.demo.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.danidevbank.demo.dto.request.ChangePasswordRequest;
import com.danidevbank.demo.dto.request.UpdatePreferencesRequest;
import com.danidevbank.demo.dto.request.UpdateProfileRequest;
import com.danidevbank.demo.dto.response.PreferencesResponse;
import com.danidevbank.demo.dto.response.ProfileResponse;
import com.danidevbank.demo.dto.response.ThemePresetResponse;
import com.danidevbank.demo.entity.User;
import com.danidevbank.demo.entity.UserPreferences;
import com.danidevbank.demo.entity.UserProfile;
import com.danidevbank.demo.exception.BadRequestException;
import com.danidevbank.demo.exception.ResourceNotFoundException;
import com.danidevbank.demo.repository.UserProfileRepository;
import com.danidevbank.demo.repository.UserRepository;
import com.danidevbank.demo.service.ProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    /** Tamaño máximo de la foto de perfil: 2 MB. */
    public static final long MAX_AVATAR_BYTES = 2L * 1024 * 1024;

    private static final String AVATAR_PATH = "/api/v1/profile/avatar";

    private static final List<ThemePresetResponse> THEME_PRESETS = List.of(
            new ThemePresetResponse("ocean", "Océano", "#2563EB", "#10B981"),
            new ThemePresetResponse("forest", "Bosque", "#15803D", "#CA8A04"),
            new ThemePresetResponse("sunset", "Atardecer", "#EA580C", "#DB2777"),
            new ThemePresetResponse("grape", "Uva", "#7C3AED", "#06B6D4"),
            new ThemePresetResponse("ruby", "Rubí", "#DC2626", "#F59E0B"),
            new ThemePresetResponse("graphite", "Grafito", "#334155", "#0EA5E9"),
            new ThemePresetResponse("coffee", "Café", "#92400E", "#65A30D"),
            new ThemePresetResponse("mint", "Menta", "#0D9488", "#8B5CF6"));

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------------- perfil

    @Override
    @Transactional
    public ProfileResponse getProfile(String email) {
        User user = findUser(email);
        return toResponse(user, getOrCreateProfile(user));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUser(email);
        UserProfile profile = getOrCreateProfile(user);

        if (request.getFirstName() != null) {
            user.setFirstName(requireText(request.getFirstName(), "El nombre no puede estar vacío"));
        }
        if (request.getLastName() != null) {
            user.setLastName(requireText(request.getLastName(), "El apellido no puede estar vacío"));
        }
        if (request.getDisplayName() != null) profile.setDisplayName(blankToNull(request.getDisplayName()));
        if (request.getPhone() != null) profile.setPhone(blankToNull(request.getPhone()));
        if (request.getBio() != null) profile.setBio(blankToNull(request.getBio()));
        if (request.getBirthDate() != null) profile.setBirthDate(request.getBirthDate());
        if (request.getCity() != null) profile.setCity(blankToNull(request.getCity()));
        if (request.getCountry() != null) profile.setCountry(blankToNull(request.getCountry()));
        if (request.getAvatarColor() != null) profile.setAvatarColor(normalizeColor(blankToNull(request.getAvatarColor())));

        userRepository.save(user);
        profileRepository.save(profile);
        return toResponse(user, profile);
    }

    // ---------------------------------------------------------- preferencias

    @Override
    @Transactional
    public PreferencesResponse updatePreferences(String email, UpdatePreferencesRequest request) {
        UserProfile profile = getOrCreateProfile(findUser(email));
        UserPreferences prefs = preferencesOf(profile);

        if (request.getThemePreset() != null) {
            ThemePresetResponse preset = THEME_PRESETS.stream()
                    .filter(p -> p.id().equalsIgnoreCase(request.getThemePreset().trim()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Tema no encontrado: " + request.getThemePreset()));
            prefs.setPrimaryColor(preset.primaryColor());
            prefs.setAccentColor(preset.accentColor());
        }
        // Colores explícitos tienen prioridad sobre el tema predefinido.
        if (request.getPrimaryColor() != null) prefs.setPrimaryColor(normalizeColor(request.getPrimaryColor()));
        if (request.getAccentColor() != null) prefs.setAccentColor(normalizeColor(request.getAccentColor()));
        if (request.getThemeMode() != null) prefs.setThemeMode(request.getThemeMode());
        if (request.getFontSize() != null) prefs.setFontSize(request.getFontSize());
        if (request.getLanguage() != null) prefs.setLanguage(request.getLanguage());
        if (request.getCurrency() != null) prefs.setCurrency(request.getCurrency());
        if (request.getHideBalances() != null) prefs.setHideBalances(request.getHideBalances());
        if (request.getCompactMode() != null) prefs.setCompactMode(request.getCompactMode());
        if (request.getEmailNotifications() != null) prefs.setEmailNotifications(request.getEmailNotifications());
        if (request.getPushNotifications() != null) prefs.setPushNotifications(request.getPushNotifications());
        if (request.getTransactionAlerts() != null) prefs.setTransactionAlerts(request.getTransactionAlerts());

        profileRepository.save(profile);
        return toPreferencesResponse(prefs);
    }

    @Override
    @Transactional
    public PreferencesResponse resetPreferences(String email) {
        UserProfile profile = getOrCreateProfile(findUser(email));
        profile.setPreferences(UserPreferences.defaults());
        profileRepository.save(profile);
        return toPreferencesResponse(profile.getPreferences());
    }

    @Override
    public List<ThemePresetResponse> getThemePresets() {
        return THEME_PRESETS;
    }

    // ------------------------------------------------------------------ foto

    @Override
    @Transactional
    public ProfileResponse uploadAvatar(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debe adjuntar una imagen");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BadRequestException("La imagen no puede superar 2 MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("No se pudo leer la imagen");
        }

        // No confiamos en el Content-Type enviado por el cliente: lo detectamos por la firma del archivo.
        String contentType = detectImageType(bytes);
        if (contentType == null) {
            throw new BadRequestException("Formato no permitido. Use JPG, PNG, WEBP o GIF");
        }

        User user = findUser(email);
        UserProfile profile = getOrCreateProfile(user);
        profile.setAvatar(bytes);
        profile.setAvatarContentType(contentType);
        profile.setAvatarUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
        return toResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AvatarData getAvatar(String email) {
        User user = findUser(email);
        UserProfile profile = profileRepository.findByUserId(user.getId())
                .filter(UserProfile::hasAvatar)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene foto de perfil"));

        long lastModified = profile.getAvatarUpdatedAt() == null ? 0L
                : profile.getAvatarUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new AvatarData(profile.getAvatar(), profile.getAvatarContentType(), lastModified);
    }

    @Override
    @Transactional
    public ProfileResponse deleteAvatar(String email) {
        User user = findUser(email);
        UserProfile profile = getOrCreateProfile(user);
        profile.setAvatar(null);
        profile.setAvatarContentType(null);
        profile.setAvatarUpdatedAt(null);
        profileRepository.save(profile);
        return toResponse(user, profile);
    }

    // ------------------------------------------------------------ seguridad

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUser(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("La confirmación no coincide con la nueva contraseña");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // -------------------------------------------------------------- helpers

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el email: " + email));
    }

    /** Los usuarios registrados antes de existir el perfil lo obtienen en su primer acceso. */
    private UserProfile getOrCreateProfile(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> profileRepository.save(UserProfile.builder()
                        .user(user)
                        .avatarColor(UserPreferences.DEFAULT_PRIMARY_COLOR)
                        .build()));
    }

    /** Filas antiguas pueden tener las columnas embebidas en null; Hibernate devuelve entonces null. */
    private UserPreferences preferencesOf(UserProfile profile) {
        if (profile.getPreferences() == null) {
            profile.setPreferences(UserPreferences.defaults());
        }
        return profile.getPreferences();
    }

    private ProfileResponse toResponse(User user, UserProfile profile) {
        boolean hasAvatar = profile.hasAvatar();
        String avatarUrl = null;
        if (hasAvatar) {
            long version = profile.getAvatarUpdatedAt() == null ? 0
                    : profile.getAvatarUpdatedAt().atZone(ZoneId.systemDefault()).toEpochSecond();
            avatarUrl = AVATAR_PATH + "?v=" + version; // el parámetro evita que el navegador use una foto vieja en caché
        }

        return ProfileResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .displayName(profile.getDisplayName() != null ? profile.getDisplayName() : user.getFirstName())
                .initials(initials(user))
                .phone(profile.getPhone())
                .bio(profile.getBio())
                .birthDate(profile.getBirthDate())
                .city(profile.getCity())
                .country(profile.getCountry())
                .hasAvatar(hasAvatar)
                .avatarUrl(avatarUrl)
                .avatarColor(profile.getAvatarColor() != null ? profile.getAvatarColor() : preferencesOf(profile).getPrimaryColor())
                .avatarUpdatedAt(profile.getAvatarUpdatedAt())
                .accountsCount(user.getAccounts() == null ? 0 : user.getAccounts().size())
                .memberSince(user.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .preferences(toPreferencesResponse(preferencesOf(profile)))
                .build();
    }

    private PreferencesResponse toPreferencesResponse(UserPreferences p) {
        UserPreferences d = UserPreferences.defaults();
        return PreferencesResponse.builder()
                .themeMode(p.getThemeMode() != null ? p.getThemeMode() : d.getThemeMode())
                .primaryColor(p.getPrimaryColor() != null ? p.getPrimaryColor() : d.getPrimaryColor())
                .accentColor(p.getAccentColor() != null ? p.getAccentColor() : d.getAccentColor())
                .fontSize(p.getFontSize() != null ? p.getFontSize() : d.getFontSize())
                .language(p.getLanguage() != null ? p.getLanguage() : d.getLanguage())
                .currency(p.getCurrency() != null ? p.getCurrency() : d.getCurrency())
                .hideBalances(Boolean.TRUE.equals(p.getHideBalances()))
                .compactMode(Boolean.TRUE.equals(p.getCompactMode()))
                .emailNotifications(!Boolean.FALSE.equals(p.getEmailNotifications()))
                .pushNotifications(!Boolean.FALSE.equals(p.getPushNotifications()))
                .transactionAlerts(!Boolean.FALSE.equals(p.getTransactionAlerts()))
                .build();
    }

    private static String initials(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) sb.append(user.getFirstName().trim().charAt(0));
        if (user.getLastName() != null && !user.getLastName().isBlank()) sb.append(user.getLastName().trim().charAt(0));
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String message) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new BadRequestException(message);
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeColor(String hex) {
        return hex == null ? null : hex.toUpperCase(Locale.ROOT);
    }

    /** Detecta el tipo de imagen por sus primeros bytes ("magic numbers"). */
    static String detectImageType(byte[] b) {
        if (b == null || b.length < 12) return null;
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return "image/gif";
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        return null;
    }
}
