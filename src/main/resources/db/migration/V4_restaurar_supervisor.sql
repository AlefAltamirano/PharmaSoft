-- Hash BCrypt generado con BCryptPasswordEncoder para Supervisor2026*.
UPDATE usuarios
SET username = 'SUPERVISOR',
    password = '$2a$10$A69XSB/v/.5mv7EOlBa7XOx6czyjM/6moNDuoksafLftHZagpr8vO',
    estado = 1
WHERE UPPER(username) = 'SUPERVISOR';

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
JOIN roles r ON r.nombre = 'VENDEDOR'
WHERE UPPER(u.username) = 'SUPERVISOR'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario_roles ur
      WHERE ur.usuario_id = u.id
        AND ur.rol_id = r.id
  );

INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u
JOIN roles r ON r.nombre = 'ALMACENERO'
WHERE UPPER(u.username) = 'SUPERVISOR'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario_roles ur
      WHERE ur.usuario_id = u.id
        AND ur.rol_id = r.id
  );
