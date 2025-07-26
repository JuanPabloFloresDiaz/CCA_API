#!/bin/bash

# Script para desarrollo local - solo base de datos

echo "🔧 Iniciando entorno de desarrollo..."

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está ejecutándose. Por favor, inicia Docker."
    exit 1
fi

# Verificar si existe el archivo .env
if [ ! -f .env ]; then
    echo "⚠️  Archivo .env no encontrado."
    echo "📋 Creando .env desde .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        echo "✅ Archivo .env creado. Por favor, revisa y ajusta las variables de entorno."
    else
        echo "❌ Error: Archivo .env.example no encontrado."
        exit 1
    fi
fi

# Detener contenedores existentes
echo "🔄 Deteniendo contenedores existentes..."
docker-compose -f compose.yaml down

# Iniciar solo la base de datos para desarrollo
echo "🗄️  Iniciando PostgreSQL para desarrollo..."
docker-compose -f compose.yaml up -d postgres

# Verificar que la base de datos esté lista
echo "⏳ Esperando que PostgreSQL esté listo..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker-compose -f compose.yaml exec -T postgres pg_isready -U postgres -d cca > /dev/null 2>&1; then
        echo "✅ PostgreSQL está listo!"
        break
    fi
    echo "⏳ Intento $attempt/$max_attempts - Esperando PostgreSQL..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo "❌ PostgreSQL no respondió después de $max_attempts intentos"
    docker-compose -f compose.yaml logs postgres
    exit 1
fi

echo ""
echo "🎉 ¡Entorno de desarrollo listo!"
echo ""
echo "📍 Base de datos disponible en:"
echo "   🗄️  Host: localhost"
echo "   🔌 Puerto: 5432"
echo "   📊 Base de datos: cca"
echo "   👤 Usuario: postgres"
echo "   🔑 Contraseña: 1234"
echo ""
echo "🚀 Para ejecutar la aplicación Spring Boot:"
echo "   mvn spring-boot:run"
echo ""
echo "🛑 Para detener PostgreSQL:"
echo "   docker-compose -f compose.yaml stop postgres"
