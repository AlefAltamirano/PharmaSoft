package pe.edu.upeu.PharmaBackckend.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==========================================
    // CambioPasswordRequestDTO Validation Tests
    // ==========================================

    @Test
    @DisplayName("CambioPasswordRequestDTO es válido con contraseña alfanumérica entre 8 y 72 caracteres")
    void cambioPasswordDTO_Valido() {
        CambioPasswordRequestDTO dto = new CambioPasswordRequestDTO("OldPass123", "NewSecretPass99");
        Set<ConstraintViolation<CambioPasswordRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("CambioPasswordRequestDTO falla si passwordActual está en blanco")
    void cambioPasswordDTO_PasswordActualEnBlanco() {
        CambioPasswordRequestDTO dto = new CambioPasswordRequestDTO("", "NewSecretPass99");
        Set<ConstraintViolation<CambioPasswordRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("La contraseña actual es obligatoria")));
    }

    @Test
    @DisplayName("CambioPasswordRequestDTO falla si passwordNueva tiene menos de 8 caracteres")
    void cambioPasswordDTO_PasswordNuevaMuyCorta() {
        CambioPasswordRequestDTO dto = new CambioPasswordRequestDTO("OldPass123", "Abc1");
        Set<ConstraintViolation<CambioPasswordRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("La contraseña debe tener entre 8 y 72 caracteres")));
    }

    @Test
    @DisplayName("CambioPasswordRequestDTO falla si passwordNueva no contiene números")
    void cambioPasswordDTO_PasswordNuevaSinNumeros() {
        CambioPasswordRequestDTO dto = new CambioPasswordRequestDTO("OldPass123", "SoloLetrasPassword");
        Set<ConstraintViolation<CambioPasswordRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("La contraseña debe contener al menos una letra y un número")));
    }

    @Test
    @DisplayName("CambioPasswordRequestDTO falla si passwordNueva no contiene letras")
    void cambioPasswordDTO_PasswordNuevaSinLetras() {
        CambioPasswordRequestDTO dto = new CambioPasswordRequestDTO("OldPass123", "1234567890");
        Set<ConstraintViolation<CambioPasswordRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("La contraseña debe contener al menos una letra y un número")));
    }

    // ==========================================
    // AsignarRolesRequestDTO Validation Tests
    // ==========================================

    @Test
    @DisplayName("AsignarRolesRequestDTO es válido con un set no vacío de roles")
    void asignarRolesDTO_Valido() {
        AsignarRolesRequestDTO dto = new AsignarRolesRequestDTO(Set.of(1L, 2L));
        Set<ConstraintViolation<AsignarRolesRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("AsignarRolesRequestDTO falla si rolIds está vacío")
    void asignarRolesDTO_Vacio() {
        AsignarRolesRequestDTO dto = new AsignarRolesRequestDTO(Collections.emptySet());
        Set<ConstraintViolation<AsignarRolesRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Debe asignar al menos un rol")));
    }

    @Test
    @DisplayName("AsignarRolesRequestDTO falla si rolIds es null")
    void asignarRolesDTO_Null() {
        AsignarRolesRequestDTO dto = new AsignarRolesRequestDTO(null);
        Set<ConstraintViolation<AsignarRolesRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Debe asignar al menos un rol")));
    }

    // ==========================================
    // EstadoUsuarioRequestDTO Validation Tests
    // ==========================================

    @Test
    @DisplayName("EstadoUsuarioRequestDTO es válido con boolean true o false")
    void estadoUsuarioDTO_Valido() {
        EstadoUsuarioRequestDTO dtoTrue = new EstadoUsuarioRequestDTO(true);
        assertTrue(validator.validate(dtoTrue).isEmpty());

        EstadoUsuarioRequestDTO dtoFalse = new EstadoUsuarioRequestDTO(false);
        assertTrue(validator.validate(dtoFalse).isEmpty());
    }

    @Test
    @DisplayName("EstadoUsuarioRequestDTO falla si estado es null")
    void estadoUsuarioDTO_Null() {
        EstadoUsuarioRequestDTO dto = new EstadoUsuarioRequestDTO(null);
        Set<ConstraintViolation<EstadoUsuarioRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("El estado es obligatorio")));
    }
}

