#!/bin/bash

# Script para probar los endpoints de Aplicaciones
# Asegúrate de que la aplicación esté corriendo en localhost:8080

BASE_URL="http://localhost:8080/api/aplicaciones"
JSON_CONTENT="Content-Type: application/json"

echo "🧪 Ejecutando tests de integración para API de Aplicaciones"
echo "=========================================================="

# Variables para capturar IDs
APLICACION_ID=""

echo ""
echo "📝 1. Crear una nueva aplicación"
echo "--------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "$JSON_CONTENT" \
  -d '{
    "nombre": "Sistema de Ventas",
    "descripcion": "Sistema principal para gestión de ventas y facturación",
    "url": "https://ventas.miempresa.com",
    "llaveIdentificadora": "VENTAS_SYS_2024",
    "estado": "ACTIVO"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 201 ]; then
    echo "✅ ÉXITO: Aplicación creada (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
    APLICACION_ID=$(echo "$BODY" | jq -r '.data.id' 2>/dev/null)
    echo "🔑 ID de aplicación capturado: $APLICACION_ID"
else
    echo "❌ ERROR: No se pudo crear la aplicación (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "📖 2. Obtener aplicación por ID"
echo "-------------------------------"
if [ ! -z "$APLICACION_ID" ] && [ "$APLICACION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/$APLICACION_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Aplicación obtenida (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo obtener la aplicación (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de aplicación para probar"
fi

echo ""
echo "🔑 3. Obtener aplicación por llave identificadora"
echo "------------------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/llave/VENTAS_SYS_2024")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Aplicación obtenida por llave (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener la aplicación por llave (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "📋 4. Obtener todas las aplicaciones"
echo "------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Lista de aplicaciones obtenida (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.data | length' 2>/dev/null | xargs echo "Cantidad de aplicaciones:"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener la lista de aplicaciones (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "📄 5. Obtener aplicaciones con paginación"
echo "-----------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/paginado?page=0&size=5")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Aplicaciones paginadas obtenidas (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.data.totalElements' 2>/dev/null | xargs echo "Total de elementos:"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener aplicaciones paginadas (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "🔍 6. Buscar aplicaciones por nombre"
echo "------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/buscar?nombre=Sistema")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Búsqueda completada (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.data | length' 2>/dev/null | xargs echo "Resultados encontrados:"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: Error en la búsqueda (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "✏️  7. Actualizar aplicación"
echo "-----------------------------"
if [ ! -z "$APLICACION_ID" ] && [ "$APLICACION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/$APLICACION_ID" \
      -H "$JSON_CONTENT" \
      -d '{
        "nombre": "Sistema de Ventas Actualizado",
        "descripcion": "Sistema principal para gestión de ventas, facturación e inventario",
        "url": "https://ventas-v2.miempresa.com",
        "llaveIdentificadora": "VENTAS_SYS_2024",
        "estado": "ACTIVO"
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Aplicación actualizada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo actualizar la aplicación (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de aplicación para actualizar"
fi

echo ""
echo "🔄 8. Cambiar estado de aplicación"
echo "----------------------------------"
if [ ! -z "$APLICACION_ID" ] && [ "$APLICACION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$APLICACION_ID/estado?estado=INACTIVO")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Estado cambiado (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo cambiar el estado (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de aplicación para cambiar estado"
fi

echo ""
echo "🔢 9. Contar aplicaciones activas"
echo "---------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/contar")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Conteo obtenido (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener el conteo (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "❓ 10. Verificar existencia por llave identificadora"
echo "---------------------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/existe/VENTAS_SYS_2024")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Verificación completada (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: Error en la verificación (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "🗑️  11. Eliminar aplicación (soft delete)"
echo "------------------------------------------"
if [ ! -z "$APLICACION_ID" ] && [ "$APLICACION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/$APLICACION_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Aplicación eliminada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo eliminar la aplicación (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de aplicación para eliminar"
fi

echo ""
echo "🔄 12. Restaurar aplicación"
echo "---------------------------"
if [ ! -z "$APLICACION_ID" ] && [ "$APLICACION_ID" != "null" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL/$APLICACION_ID/restaurar")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Aplicación restaurada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo restaurar la aplicación (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de aplicación para restaurar"
fi

echo ""
echo "🎯 13. Probar error de validación"
echo "----------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "$JSON_CONTENT" \
  -d '{
    "nombre": "",
    "descripcion": "Aplicación sin nombre",
    "url": "invalid-url",
    "llaveIdentificadora": "abc"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 400 ]; then
    echo "✅ ÉXITO: Error de validación capturado correctamente (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: Debería haber fallado la validación (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "🎉 RESUMEN DE TESTS COMPLETADO"
echo "================================"
echo "✅ Tests de Aplicaciones ejecutados exitosamente"
echo "📊 Revisa los resultados arriba para verificar el comportamiento"
echo "🌐 Swagger UI disponible en: http://localhost:8080/swagger-ui.html"
echo ""
