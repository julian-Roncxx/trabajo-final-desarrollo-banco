package com.danidevbank.demo.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.danidevbank.demo.dto.request.ChangePasswordRequest;
import com.danidevbank.demo.dto.request.UpdatePreferencesRequest;
import com.danidevbank.demo.dto.request.UpdateProfileRequest;
import com.danidevbank.demo.dto.response.PreferencesResponse;
import com.danidevbank.demo.dto.response.ProfileResponse;
import com.danidevbank.demo.dto.response.ThemePresetResponse;

public interface ProfileService {

    ProfileResponse getProfile(String email);

    ProfileResponse updateProfile(String email, UpdateProfileRequest request);

    PreferencesResponse updatePreferences(String email, UpdatePreferencesRequest request);

    PreferencesResponse resetPreferences(String email);

    List<ThemePresetResponse> getThemePresets();

    ProfileResponse uploadAvatar(String email, MultipartFile file);

    AvatarData getAvatar(String email);

    ProfileResponse deleteAvatar(String email);

    void changePassword(String email, ChangePasswordRequest request);

    /** Imagen de perfil lista para enviarse en la respuesta HTTP. */
    record AvatarData(byte[] content, String contentType, long lastModifiedEpochMs) {
    }
}
