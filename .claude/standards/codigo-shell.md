# Código Shell — Reglas para scripts ksh/sh en portales OXXO/FEMSA

> Consolida **Documento de Estándares 5.0 §6.3.5** (Codificación Shell).
>
> Cita: *"viola Doc Estándares 5.0 §6.3.5"*.
>
> **Aplica si el proyecto incluye scripts shell ejecutados por Control-M o como jobs del backend.** Para portales que no tienen jobs shell, este archivo es informativo.

---

## Contexto

- **OS:** Red Hat Enterprise Linux (RHEL 6.5+).
- **Shell soportados:** `sh` y `ksh` (NO bash-isms).
- **Scheduler:** Control-M ejecuta los jobs según ventanas configuradas.
- **Permisos en servidor:** `chmod 740` (rwxr-----) — el archivo de instalación debe pedirlo.
- **Ubicación estándar:** `/home/bportal/bin/` (modificable según arquitectura del proyecto).
- **Servidores típicos:**
  - DEV: `oxfbdfciasd00.femcom.net`
  - QA: `oxfbdfciasq00.femcom.net`
  - PRD: `OXFBDFCIASP1.FEMCOM.NET` + `OXFBDFCIASP2.FEMCOM.NET` (RAC)

---

## §1. Header obligatorio

```bash
#!/bin/ksh
###############################################################################
# <nombreScript>.sh <version> <DD/MM/YY>
#
# Copyright <YYYY> FEMSA Comercio, OXXO. All rights reserved.
# OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#
# Autor: <nombre>, <equipo>
#
# Descripcion: <qué hace este script>
#
#-----------------------------------------------------------------------------
# Historia de Modificaciones
# =============================================================================
# Autor          Fecha         Descripcion
# -------------- ------------- ------------------------------------------------
# HMA            2017-11-28    Version inicial
# <nombre>       2026-05-19    <descripción del cambio>
###############################################################################
```

## §2. Llamado al archivo de variables de ambiente

Después del header:
```bash
. $HOME/env10g.env
```

> **Nota:** el nombre `env10g.env` es histórico (Oracle 10g). Verificar con el EDA cuál es el archivo actual del proyecto antes de cablearlo. En proyectos nuevos puede ser `env.sh`, `env_portal.env`, etc.

🔴 **Variables sensibles (conexión a BD, passwords, tokens) DEBEN venir del archivo de ambiente, NO hardcoded en el script.**

## §3. Declaración de variables locales

Después de cargar el ambiente, declarar variables del script con comentarios:

```bash
# variable de cantidad de opciones disponibles
opcion=2

# variable que contiene el url a ser llamado
VURL="$PORTAL_SAR3_JOB_IP/SAR3/ejecutarActualizacionIEPS"
```

## §4. Echo para visibilidad en Control-M

Imprimir mensaje de arranque para que Control-M lo capture en su output:
```bash
echo "Inicia proceso de envio de correo para los codigos SAT nuevos"
```

🔴 **NUNCA imprimir credenciales en echo:**
```bash
# ❌ PROHIBIDO
echo "Password $password $username"

# ✅ Permitido (sin info sensible)
echo "Iniciando conexión con usuario $username"  # sin password
echo "Conectando a BD: $DB_HOST"                 # host es OK, no password
```

## §5. Validación del retorno de comandos externos

Para `wget`/`curl`/cualquier llamada externa, validar `$?`:

```bash
wget -O- ${PORTAL_CATALOGOS_CORREO_JOB_IP}/portalFiscalizacion-web/envioCorreo
if [ $? -ne 0 ]; then
    echo "Error en la ejecución del proceso"
    exit 1
else
    echo "Fin de proceso exitoso"
fi
```

## §6. Indentación e legibilidad

**4 espacios entre sentencias e instrucciones.**

```bash
if [[ $# -eq 0 ]]; then
    opcion=2
else
    opcion=$1
fi
```

**Espacio en blanco entre sentencias y variables a validar:**
```bash
while [ "$number" -lt 10 ]; do
    echo "Number = $number"
    number=$((number + 1))
done
```

## §7. Recepción de parámetros desde Control-M

Control-M puede pasar parámetros:
```bash
# Invocación desde Control-M:
# sh scriptShell.sh parametro1 parametro2

# Dentro del script:
variable1=$1
variable2=$2

# Validar que vinieron
if [ -z "$variable1" ]; then
    echo "Error: falta parametro1"
    exit 1
fi
```

## §8. Códigos de retorno

🔴 **OBLIGATORIO** terminar con `exit 0` (éxito) o `exit 1` (error) para que Control-M sepa qué pasó:

```bash
# Buena ejecución
exit 0

# Mala ejecución
exit 1
```

Sin `exit` explícito, el código de retorno es indefinido y Control-M puede marcar el job como exitoso aunque haya fallado.

## §9. Conexión a Base de Datos

Si el script llama a `sqlplus`:

```bash
sqlplus $CONNECT_STRING_PFE > $SCRIPT_OUTPUT <<EOF
SET PAGESIZE 0
SET FEEDBACK OFF
SELECT COUNT(*) FROM XXVA_ENG_PLANOGRAMA_APROBADO WHERE FECHA = TRUNC(SYSDATE);
EXIT;
EOF
```

🔴 **Obligatorio:** redirigir stdout a un archivo de log (`> $SCRIPT_OUTPUT`).

🔴 **Donde sea posible:** validar el resultado de sqlplus antes de continuar.

## §10. Locación de archivos

Las rutas no se hardcodean — se ponen en variables al inicio del script, idealmente desde el archivo de ambiente:

```bash
# ❌ MAL
mv /u11/usrplan/csv/$fileName /u11/usrplan/.usrplan/csv/read/

# ✅ BIEN
mv $INPUT_DIR/$fileName $READ_DIR/
```

Las carpetas concretas se acuerdan entre EDA y L3.

## §11. Manejo de archivos lock (evitar doble ejecución)

Patrón usado en `starMonitor` (Planogramas Carga Diaria):

```bash
LOCK_FILE="/var/lock/mi_script.lock"

if [ -f "$LOCK_FILE" ]; then
    echo "Proceso ya está corriendo, abortando"
    exit 1
fi

trap "rm -f $LOCK_FILE" EXIT  # eliminar lock al salir, aún si hay error
touch "$LOCK_FILE"

# ... resto del script ...
```

---

## Checklist rápido para code review

- [ ] Header completo con autor, fecha, descripción y modificaciones
- [ ] Llamado al archivo de ambiente (`. $HOME/env*.env`)
- [ ] Variables sensibles vienen del ambiente, no hardcoded
- [ ] Echos en puntos clave para Control-M
- [ ] NO hay echos con passwords/tokens
- [ ] Cada llamada externa valida `$?`
- [ ] Indentación de 4 espacios
- [ ] `exit 0` / `exit 1` explícitos
- [ ] Si conecta a BD: salida redirigida a log
- [ ] Si tiene lock file: trap para limpiarlo en error

---

## Referencias cruzadas
- `codigo-java.md` — si el script invoca endpoints REST del backend
- `codigo-plsql.md` — si el script ejecuta SQL contra Oracle
- `entregables-portal.md` — los scripts shell se referencian en Esp Técnica §1.6 (Información de ejecución)
