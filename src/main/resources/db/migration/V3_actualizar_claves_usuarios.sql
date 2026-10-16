-- Hashes generados con org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
-- usando el costo predeterminado del bean PasswordEncoder de SecurityConfig.
UPDATE usuarios
SET username = 'ADMIN',
    password = '$2a$10$7Axc6WtRTSUG8G4F/75A7.nvZgkuEaWcTTh/7hr9hRP9h7rmYIpM2'
WHERE UPPER(username) = 'ADMIN';

UPDATE usuarios
SET username = 'VENDEDOR',
    password = '$2a$10$loUkpIfRQW2RuSF6CkRx6eJ.VVgNbGX3dKhAiL5KzAGWrES.Kcgay'
WHERE UPPER(username) = 'VENDEDOR';

UPDATE usuarios
SET username = 'ALMACEN',
    password = '$2a$10$rHDHQIld4mYzswUUZ.7sxutm8N5s81y8RvWvXmxAMknRvlteHoBkm'
WHERE UPPER(username) = 'ALMACEN';
