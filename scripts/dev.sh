#!/bin/bash

# Script para desarrollo local - solo base de datos

echo "🔧 Iniciando entorno de desarrollo..."

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está ejecutándose. Por favor, inicia Docker."
    exit 1
fi

# Verificar si existe el archivo .env para desarrollo
if [ ! -f .env.development ]; then
    echo "⚠️  Archivo .env.development no encontrado."
    echo "📋 Creando .env.development desde .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env.development
        echo "✅ Archivo .env.development creado."
        echo "📝 Ajusta las configuraciones para desarrollo local (DATABASE_HOST=localhost, JPA_SHOW_SQL=true, etc.)"
    else
        echo "❌ Error: Archivo .env.example no encontrado."
        exit 1
    fi
fi

# Copiar configuración de desarrollo como .env para docker-compose
cp .env.development .env

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
echo "   🔌 Puerto: ${DB_EXTERNAL_PORT:-5432}"
echo "   📊 Base de datos: ${DATABASE_NAME:-cca}"
echo "   👤 Usuario: ${DATABASE_USERNAME:-postgres}"
echo "   🔑 Contraseña: ${DATABASE_PASSWORD:-1234}"
echo ""
echo "🚀 Para ejecutar la aplicación Spring Boot:"
echo "   mvn spring-boot:run"
echo ""
echo "📝 Configuración utilizada: .env.development"
echo ""
echo "🛑 Para detener PostgreSQL:"
echo "   docker-compose -f compose.yaml stop postgres"
