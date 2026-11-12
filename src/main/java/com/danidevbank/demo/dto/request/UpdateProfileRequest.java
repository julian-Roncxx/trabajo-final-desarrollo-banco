package com.danidevbank.demo.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualización parcial de los datos del perfil. Los campos que lleguen en null
 * no se modifican; para borrar un campo de texto envíe una cadena vacía "".
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
    private String firstName;

    @Size(min = 1, max = 50, message = "El apellido debe tener entre 1 y 50 caracteres")
    private String lastName;

    @Size(max = 50, message = "El nombre visible no puede superar 50 caracteres")
    private String displayName;

    @Pattern(regexp = "^$|^\\+?[0-9 ()-]{7,20}$", message = "El teléfono no es válido")
    private String phone;

    @Size(max = 250, message = "La biografía no puede superar 250 caracteres")
    private String bio;

    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate birthDate;

    @Size(max = 60, message = "La ciudad no puede superar 60 caracteres")
    private String city;

    @Size(max = 60, message = "El país no puede superar 60 caracteres")
    private String country;

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "El color del avatar debe tener formato #RRGGBB")
    private String avatarColor;
}
