# Código PL/SQL — Reglas compactas para portales OXXO/FEMSA

> Consolida **STTI Estándar de Codificación Segura PL/SQL v2** (Ene 2023) + **Documento de Estándares 5.0 §6.4** (nombrado de objetos BD).
>
> Secciones (§) preservan numeración del estándar original. Cita: *"viola STTI PL/SQL v2 §3.2"*.
>
> **Aplica si `stack.database = oracle` en project-config.yml.** Para PostgreSQL, los principios anti-inyección aplican pero la sintaxis cambia.

---

## §1. Introducción
Minimizar vulnerabilidades en código PL/SQL ejecutado en BD Oracle de los portales FEMSA.

## §2. Nombrado de objetos BD (Doc Estándares 5.0 §6.4.1)

**Prefijo de esquema:** todos los objetos llevan el prefijo del esquema/usuario propietario. El prefijo empieza con `XX` seguido de 2-3 letras del proyecto.

Ejemplos de esquemas oficiales:
- `XXVA` — Sistema de Variedad
- `XXMWF` — Sistema Megaworkflow de Expansión
- `XXENG` — Engineering / Planogramas
- `XXMDM` — Master Data Management

**Tipos de objetos y sufijos:**

| Tipo | Sufijo | Ejemplo |
|---|---|---|
| Tabla | (sin sufijo) | `XXVA_ENG_PLANOGRAMA_APROBADO` |
| Vista | `_V` | `XXVA_RMS_ITEM_MASTER_V` |
| Vista materializada | `_MV` | `XXVA_RMS_ESTRUCTURA_CAT_ITM_MV` |
| Índice | `_IDX` o `_PK` (PK) / `_FK` (FK) / `_UK` (unique) | `XXVA_PLANOGRAMA_PK` |
| Secuencia | `_SEQ` | `XXVA_PLANOGRAMA_SEQ` |
| Trigger | `_TRG` | `XXVA_ENG_PLANOGRAMA_HIS_TRG` |
| Paquete | `_PK` o `_PKG` | `XXVA_PLANOGRAMAS_M_PK` |
| Procedimiento | (descriptivo) | `MONITORING_STEP_1` |
| Función | (descriptivo) | `GET_ITEM_DESC` |

**Permisos otorgados al usuario aplicativo:** mínimos necesarios. Para cada objeto en la Esp Técnica §4.6, listar: tabla/vista, esquema, permiso (SELECT / INSERT / UPDATE / DELETE / EXECUTE).

---

## §3. SQL Injection — Texto estático vs dinámico

### §3.1 Texto estático (siempre seguro)
```sql
-- ✅ Variables bind: el parser de Oracle nunca interpreta v_user_id como SQL
DECLARE
    v_user_id NUMBER := 42;
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM users
    WHERE id = v_user_id;
END;
```

### §3.2 Texto dinámico (PELIGROSO si mal hecho)

🔴 **NUNCA concatenar input del usuario en SQL dinámico.**

```plsql
-- ❌ VULNERABLE — SQL injection trivial
CREATE OR REPLACE PROCEDURE buscar_usuario(p_email VARCHAR2) IS
    v_sql VARCHAR2(1000);
BEGIN
    v_sql := 'SELECT * FROM users WHERE email = ''' || p_email || '''';
    EXECUTE IMMEDIATE v_sql;
END;
-- Si p_email = ''' OR '1'='1' --
-- → SELECT * FROM users WHERE email = '' OR '1'='1' -- '
```

### §3.3 Texto dinámico seguro (BIND VARIABLES)

✅ **Patrón correcto:**
```plsql
CREATE OR REPLACE PROCEDURE buscar_usuario(p_email VARCHAR2) IS
    v_sql VARCHAR2(1000);
    v_resultado users%ROWTYPE;
BEGIN
    v_sql := 'SELECT * FROM users WHERE email = :1';
    EXECUTE IMMEDIATE v_sql INTO v_resultado USING p_email;
END;
```

### §3.4 DBMS_SQL (alternativa para SQL muy dinámico)

Cuando ni siquiera la estructura del query es conocida en compile-time:
```plsql
DECLARE
    v_cursor NUMBER;
    v_rows NUMBER;
BEGIN
    v_cursor := DBMS_SQL.OPEN_CURSOR;
    DBMS_SQL.PARSE(v_cursor, 'SELECT * FROM users WHERE id = :id', DBMS_SQL.NATIVE);
    DBMS_SQL.BIND_VARIABLE(v_cursor, ':id', p_user_id);
    v_rows := DBMS_SQL.EXECUTE(v_cursor);
    -- ...
    DBMS_SQL.CLOSE_CURSOR(v_cursor);
END;
```

### §3.5 Validación de identificadores dinámicos

Si necesitas SQL dinámico donde el NOMBRE DE TABLA o COLUMNA viene del usuario (caso muy raro), validar contra lista blanca:

```plsql
DECLARE
    v_tabla VARCHAR2(30);
BEGIN
    -- ✅ Validar contra lista blanca antes de usar
    IF p_tabla NOT IN ('USERS', 'ORDERS', 'PRODUCTS') THEN
        RAISE_APPLICATION_ERROR(-20001, 'Tabla no permitida');
    END IF;
    v_tabla := DBMS_ASSERT.SIMPLE_SQL_NAME(p_tabla);  -- segunda validación
    EXECUTE IMMEDIATE 'SELECT COUNT(*) FROM ' || v_tabla INTO v_count;
END;
```

`DBMS_ASSERT` provee validadores: `SIMPLE_SQL_NAME`, `QUALIFIED_SQL_NAME`, `SCHEMA_NAME`, `ENQUOTE_LITERAL`, `ENQUOTE_NAME`.

---

## §4. Principios complementarios

### §4.1 Cursores: SIEMPRE cerrar

```plsql
DECLARE
    CURSOR c_users IS SELECT id, email FROM users WHERE activo = 'Y';
    v_id users.id%TYPE;
    v_email users.email%TYPE;
BEGIN
    OPEN c_users;
    LOOP
        FETCH c_users INTO v_id, v_email;
        EXIT WHEN c_users%NOTFOUND;
        -- procesar
    END LOOP;
    CLOSE c_users;  -- ✅ obligatorio
EXCEPTION
    WHEN OTHERS THEN
        IF c_users%ISOPEN THEN CLOSE c_users; END IF;  -- ✅ cerrar también en error
        RAISE;
END;
```

### §4.2 Manejo de excepciones

Capturar `WHEN OTHERS` y registrar en tabla de logs (`XXVA_ENG_LOGS` o equivalente), pero NUNCA silenciar:

```plsql
EXCEPTION
    WHEN OTHERS THEN
        INSERT INTO XXVA_ENG_LOGS (timestamp, modulo, mensaje, error_code)
        VALUES (SYSDATE, 'mi_procedimiento', SUBSTR(SQLERRM, 1, 4000), SQLCODE);
        COMMIT;
        RAISE;  -- ✅ re-lanzar; no silenciar
END;
```

### §4.3 Privilegios mínimos

El usuario aplicativo del portal NUNCA debe tener:
- `DBA`, `SYSDBA`, `RESOURCE` ilimitado
- Permisos `ALL PRIVILEGES`
- `DROP ANY *`, `CREATE ANY *`

Solo los GRANTs mínimos sobre los objetos específicos del proyecto.

### §4.4 No registrar información altamente confidencial

Igual que en Java (CWE-117): nunca loggear contraseñas, tarjetas, PII en tablas de logs.

### §4.5 Connection pooling correcto

Para portales Spring Boot que llaman a Oracle:
- `ojdbc11` (compatible con Java 11+)
- HikariCP con `connection-test-query=SELECT 1 FROM DUAL`
- `oracle.jdbc.ReadTimeout` configurado (no infinito)

---

## §5. Estructura de paquetes (Doc Estándares 5.0 §6.4.3)

Cada paquete debe tener:

**SPEC (`*.pks`):** declara procedimientos/funciones públicos + tipos compartidos.
```plsql
CREATE OR REPLACE PACKAGE XXVA_USUARIO_PKG AS
    PROCEDURE crear_usuario(p_email IN VARCHAR2, p_id OUT NUMBER);
    FUNCTION buscar_por_email(p_email IN VARCHAR2) RETURN users%ROWTYPE;
END XXVA_USUARIO_PKG;
```

**BODY (`*.pkb`):** implementación + procedimientos/funciones privados.

**Comentario de header obligatorio:**
```plsql
-- ###########################################################
-- # Package: XXVA_USUARIO_PKG
-- # Author: <nombre>
-- # Created: <DD/MM/YYYY>
-- # Description: <qué hace este paquete>
-- # Modifications:
-- #   YYYY-MM-DD - <autor> - <descripción>
-- ###########################################################
```

---

## §6. Migraciones (Flyway / Liquibase / scripts manuales)

Los scripts DDL viven en el repo del backend bajo `src/main/resources/db/migration/`.

**Naming Flyway:**
```
V1.0.0__crear_tabla_usuarios.sql
V1.0.1__agregar_indice_email_usuarios.sql
V1.1.0__agregar_tabla_pedidos.sql
```

🔴 Una vez aplicado a DEV/QA/PROD, **un script NO se modifica**. Solo se agrega uno nuevo que corrija.

---

## §7. Conexiones a BD según ambiente

Ver `.claude/project-config.yml`. Las URLs y credenciales vienen de Azure Key Vault como `${VAR}` — nunca hardcoded.

Hosts típicos de portales EDT Comercial (según WEB Training Material v3):
- DEV: `oxvwfed1.femcom.net` (Expandes), `oxfbdfciasd00.femcom.net` (FCIAS), etc.
- QA, PRD: gestionados por DBA / CMS

El LT solicita via ticket a DBA si necesita nuevo schema o nueva BD.

---

## Referencias cruzadas
- `codigo-java.md` §3 — SQL injection desde la capa Java
- `seguridad-checklist.md` — OWASP A03 Injection
- `entregables-portal.md` — proceso para nueva BD (DBA + Esp Técnica §4.6)
