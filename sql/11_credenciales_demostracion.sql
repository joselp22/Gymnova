-- Credenciales oficiales de demostracion GYMNOVA (agosto 2026).
-- Reejecutar este script restaura las cinco cuentas de rol a un estado limpio
-- (contrasena Gymnova123*, sin bloqueos, sin intentos fallidos).
--
-- Administrador principal: usuario 'jose' con contrasena 'Admin123*'.
--   NO se modifica desde este script para preservar la cuenta personal.
--
-- Cuentas de rol para la demostracion (contrasena unica: Gymnova123*):
--   RECEPCION    (Recepcionista)
--   ENTRENADOR   (Entrenador)
--   NUTRICION    (Nutricionista)
--   CLIENTE      (Cliente)
--
-- El hash es PBKDF2WithHmacSHA256, 210000 iteraciones, sal 16 bytes.
-- Fue generado con utilidades.SeguridadClave.generarHash("Gymnova123*").

BEGIN;

UPDATE usuario
SET clave_hash = '210000:ad1437156be6754f4b566279bf7b1dda:59ac2c0eae54cd2983e26987af10ca9b6c723250644cc5a7764af6084b344b77',
    bloqueado = FALSE,
    intentos_fallidos = 0,
    estado_usuario = TRUE
WHERE nombre_usuario IN ('RECEPCION', 'ENTRENADOR', 'NUTRICION', 'CLIENTE');

-- Se desactiva el usuario 'admin' con hash placeholder invalido.
-- El administrador real es 'jose'.
UPDATE usuario
SET estado_usuario = FALSE, bloqueado = TRUE
WHERE nombre_usuario = 'admin';

COMMIT;

-- Verificacion:
--   SELECT nombre_usuario, estado_usuario, bloqueado, intentos_fallidos
--   FROM usuario ORDER BY id_usuario;
