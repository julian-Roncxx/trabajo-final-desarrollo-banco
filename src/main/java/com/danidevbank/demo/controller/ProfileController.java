package com.danidevbank.demo.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.danidevbank.demo.dto.request.ChangePasswordRequest;
import com.danidevbank.demo.dto.request.UpdatePreferencesRequest;
import com.danidevbank.demo.dto.request.UpdateProfileRequest;
import com.danidevbank.demo.dto.response.MessageResponse;
import com.danidevbank.demo.dto.response.PreferencesResponse;
import com.danidevbank.demo.dto.response.ProfileResponse;
import com.danidevbank.demo.dto.response.ThemePresetResponse;
import com.danidevbank.demo.service.ProfileService;
import com.danidevbank.demo.service.ProfileService.AvatarData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Perfil", description = "Datos personales, foto de perfil y personalización de la interfaz")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Obtener el perfil del usuario autenticado (incluye preferencias)")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @Operation(summary = "Actualizar datos personales (actualización parcial: los campos omitidos no cambian)")
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }

    // ------------------------------------------------------------ preferencias

    @Operation(summary = "Obtener solo las preferencias de interfaz")
    @GetMapping("/preferences")
    public ResponseEntity<PreferencesResponse> getPreferences(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()).getPreferences());
    }

    @Operation(summary = "Cambiar colores, modo claro/oscuro, idioma, tamaño de letra, notificaciones...")
    @PatchMapping("/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.updatePreferences(authentication.getName(), request));
    }

    @Operation(summary = "Restablecer las preferencias a los valores por defecto")
    @PostMapping("/preferences/reset")
    public ResponseEntity<PreferencesResponse> resetPreferences(Authentication authentication) {
        return ResponseEntity.ok(profileService.resetPreferences(authentication.getName()));
    }

    @Operation(summary = "Listar los temas de color predefinidos")
    @GetMapping("/themes")
    public ResponseEntity<List<ThemePresetResponse>> getThemes() {
        return ResponseEntity.ok(profileService.getThemePresets());
    }

    // -------------------------------------------------------------------- foto

    @Operation(summary = "Subir o reemplazar la foto de perfil (JPG, PNG, WEBP o GIF, máx. 2 MB)")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(profileService.uploadAvatar(authentication.getName(), file));
    }

    @Operation(summary = "Descargar la foto de perfil")
    @GetMapping("/avatar")
    public ResponseEntity<byte[]> getAvatar(Authentication authentication) {
        AvatarData avatar = profileService.getAvatar(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.contentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .lastModified(avatar.lastModifiedEpochMs())
                .header("X-Content-Type-Options", "nosniff")
                .body(avatar.content());
    }

    @Operation(summary = "Eliminar la foto de perfil (se vuelven a mostrar las iniciales)")
    @DeleteMapping("/avatar")
    public ResponseEntity<ProfileResponse> deleteAvatar(Authentication authentication) {
        return ResponseEntity.ok(profileService.deleteAvatar(authentication.getName()));
    }

    // --------------------------------------------------------------- seguridad

    @Operation(summary = "Cambiar la contraseña")
    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        profileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente"));
    }
}
