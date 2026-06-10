#!/usr/bin/env bash
# =====================================================================
#  mock-admin.sh — CRUD de escenarios del mock Spin vía la admin API de
#  WireMock. Cada "escenario" es un stub mapping administrable en caliente.
#
#  BASE = URL del admin de WireMock (default localhost:8082, p.ej. tras
#         'oc port-forward deploy/spin-mock 8082:8080' o docker-compose).
#
#  Uso:
#    BASE=http://localhost:8082 ./mock-admin.sh list
#    ./mock-admin.sh get <id>
#    ./mock-admin.sh add  escenario.json          # crear escenario
#    ./mock-admin.sh update <id> escenario.json   # modificar escenario
#    ./mock-admin.sh delete <id>                  # borrar escenario
#    ./mock-admin.sh reset                        # recargar desde archivos mappings/
#    ./mock-admin.sh save                         # persistir los stubs actuales a archivos
# =====================================================================
set -euo pipefail
BASE="${BASE:-http://localhost:8082}"
ADMIN="$BASE/__admin/mappings"
cmd="${1:-list}"

case "$cmd" in
  list)
    # Tabla de escenarios: id | metodo | url | status | prioridad
    curl -s "$ADMIN" | jq -r '.mappings[] | [.id, .request.method, (.request.urlPath // .request.url // "-"), (.response.status|tostring), ((.priority // 5)|tostring)] | @tsv' \
      | awk 'BEGIN{printf "%-38s %-6s %-22s %-6s %-4s\n","ID","MET","URL","HTTP","PRIO"} {printf "%-38s %-6s %-22s %-6s %-4s\n",$1,$2,$3,$4,$5}'
    ;;
  get)    curl -s "$ADMIN/${2:?id requerido}" | jq . ;;
  add)    curl -s -X POST "$ADMIN" -H "Content-Type: application/json" -d @"${2:?archivo.json requerido}" | jq '{id, priority, status:.response.status}' ;;
  update) curl -s -X PUT  "$ADMIN/${2:?id requerido}" -H "Content-Type: application/json" -d @"${3:?archivo.json requerido}" | jq '{id, priority}' ;;
  delete) curl -s -X DELETE "$ADMIN/${2:?id requerido}" -o /dev/null -w "deleted %{http_code}\n" ;;
  reset)  curl -s -X POST "$ADMIN/reset" -o /dev/null -w "reset %{http_code}\n" ;;
  save)   curl -s -X POST "$ADMIN/save"  -o /dev/null -w "saved %{http_code}\n" ;;
  *) echo "comando no reconocido: $cmd"; exit 1 ;;
esac
