-- Usuarios destinados exclusivamente a la demostracion universitaria.
-- No modifica la cuenta personal "jose".
BEGIN;
UPDATE usuario
SET clave_hash='210000:80a6c2cb9342b08881dc94d483aa27ac:3c75b4c6b7482dcefb5621495c937cfe4f5cba58fc3c555bd87bbfcb3e3f1e3e',
    bloqueado=FALSE,intentos_fallidos=0,estado_usuario=TRUE
WHERE nombre_usuario IN ('admin','RECEPCION','ENTRENADOR','NUTRICION','cliente');
COMMIT;

-- Clave de estas cinco cuentas: Gymnova2026*
