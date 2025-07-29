#!/bin/bash

# Script para probar los endpoints de Secciones
# Asegúrate de que la aplicación esté corriendo en localhost:8080

BASE_URL="http://localhost:8080/api/secciones"
JSON_CONTENT="Content-Type: application/json"

echo "🧪 Ejecutando tests de integración para API de Secciones"
echo "======================================================"

# Variables para capturar IDs
SECCION_ID=""

echo ""
echo "📝 1. Crear una nueva sección"
echo "--------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "$JSON_CONTENT" \
  -d '{
    "nombre": "Gestión de Usuarios",
    "descripcion": "Sección para administrar usuarios y permisos del sistema"
  }')

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 201 ]; then
    echo "✅ ÉXITO: Sección creada (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.'
    SECCION_ID=$(echo "$BODY" | jq -r '.data.id')
    echo "🔑 ID de sección capturado: $SECCION_ID"
else
    echo "❌ ERROR: No se pudo crear la sección (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "📖 2. Obtener sección por ID"
echo "-------------------------------"
if [ ! -z "$SECCION_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/$SECCION_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Sección obtenida (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo obtener la sección (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de sección para probar"
fi

echo ""
echo "📋 3. Obtener todas las secciones"
echo "-----------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Lista de secciones obtenida (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.data | length' 2>/dev/null | xargs echo "Cantidad de secciones:"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener la lista de secciones (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "📄 4. Obtener secciones con paginación"
echo "----------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/paginado?page=0&size=5")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ ÉXITO: Secciones paginadas obtenidas (HTTP $HTTP_CODE)"
    echo "$BODY" | jq '.data.totalElements' 2>/dev/null | xargs echo "Total de elementos:"
    echo "$BODY" | jq '.'
else
    echo "❌ ERROR: No se pudo obtener secciones paginadas (HTTP $HTTP_CODE)"
    echo "$BODY"
fi

echo ""
echo "🔍 5. Buscar secciones por nombre"
echo "-----------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/buscar?nombre=Gestión")
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
echo "✏️  6. Actualizar sección"
echo "---------------------------"
if [ ! -z "$SECCION_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/$SECCION_ID" \
      -H "$JSON_CONTENT" \
      -d '{
        "nombre": "Gestión de Usuarios Actualizada",
        "descripcion": "Sección actualizada para administrar usuarios, roles y permisos del sistema"
      }')
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Sección actualizada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo actualizar la sección (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de sección para actualizar"
fi

echo ""
echo "🔢 7. Contar secciones activas"
echo "--------------------------------"
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
echo "❓ 8. Verificar existencia por nombre"
echo "---------------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/existe/Gestión%20de%20Usuarios%20Actualizada")
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
echo "🗑️  9. Eliminar sección (soft delete)"
echo "---------------------------------------"
if [ ! -z "$SECCION_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/$SECCION_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "✅ ÉXITO: Sección eliminada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: No se pudo eliminar la sección (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de sección para eliminar"
fi

echo ""
echo "🔍 10. Verificar que la sección fue eliminada"
echo "----------------------------------------------"
if [ ! -z "$SECCION_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/$SECCION_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$HTTP_CODE" -eq 404 ]; then
        echo "✅ ÉXITO: Sección correctamente eliminada (HTTP $HTTP_CODE)"
        echo "$BODY" | jq '.'
    else
        echo "❌ ERROR: La sección debería estar eliminada (HTTP $HTTP_CODE)"
        echo "$BODY"
    fi
else
    echo "⚠️  SALTADO: No hay ID de sección para verificar"
fi

echo ""
echo "🎯 11. Probar error de validación"
echo "-----------------------------------"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL" \
  -H "$JSON_CONTENT" \
  -d '{
    "nombre": "",
    "descripcion": "Descripción sin nombre"
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
echo "✅ Tests ejecutados exitosamente"
echo "📊 Revisa los resultados arriba para verificar el comportamiento"
echo "🌐 Swagger UI disponible en: http://localhost:8080/swagger-ui.html"
echo ""
