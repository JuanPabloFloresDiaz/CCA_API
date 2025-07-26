#!/bin/bash

# Script para construir y ejecutar la aplicación con Docker Compose

echo "🏗️  Construyendo y ejecutando Centro de Control de Acceso..."

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
        echo "✅ Archivo .env creado. Por favor, revisa y ajusta las variables de entorno antes de continuar."
        echo "📝 Editando .env con nano..."
        read -p "¿Deseas editar el archivo .env ahora? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            nano .env
        fi
    else
        echo "❌ Error: Archivo .env.example no encontrado."
        exit 1
    fi
fi

# Función para limpiar recursos
cleanup() {
    echo "🧹 Limpiando recursos..."
    docker-compose -f compose.yaml down
}

# Manejar señales de interrupción
trap cleanup EXIT

# Verificar si hay contenedores ejecutándose
if docker-compose -f compose.yaml ps -q | grep -q .; then
    echo "🔄 Deteniendo contenedores existentes..."
    docker-compose -f compose.yaml down
fi

# Construir y ejecutar los servicios
echo "🚀 Iniciando servicios..."
docker-compose -f compose.yaml up --build -d

# Verificar si los servicios están corriendo
echo "⏳ Esperando que los servicios estén listos..."
sleep 10

# Verificar el estado de los servicios
echo "📊 Estado de los servicios:"
docker-compose -f compose.yaml ps

# Verificar la salud de la base de datos
echo "🔍 Verificando conectividad de la base de datos..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker-compose -f compose.yaml exec -T postgres pg_isready -U postgres -d cca > /dev/null 2>&1; then
        echo "✅ Base de datos está lista!"
        break
    fi
    echo "⏳ Intento $attempt/$max_attempts - Esperando que la base de datos esté lista..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo "❌ La base de datos no respondió después de $max_attempts intentos"
    docker-compose -f compose.yaml logs postgres
    exit 1
fi

# Verificar la salud de la API
echo "🔍 Verificando la API..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ API está lista!"
        break
    fi
    echo "⏳ Intento $attempt/$max_attempts - Esperando que la API esté lista..."
    sleep 3
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo "❌ La API no respondió después de $max_attempts intentos"
    echo "📝 Logs de la API:"
    docker-compose -f compose.yaml logs api
    exit 1
fi

echo ""
echo "🎉 ¡Aplicación desplegada exitosamente!"
echo ""
echo "📍 Endpoints disponibles:"
echo "   🌐 API: http://localhost:8080"
echo "   ❤️  Health Check: http://localhost:8080/actuator/health"
echo "   🗄️  Base de datos: localhost:5432"
echo ""
echo "📝 Para ver los logs en tiempo real:"
echo "   docker-compose -f compose.yaml logs -f"
echo ""
echo "🛑 Para detener la aplicación:"
echo "   docker-compose -f compose.yaml down"
echo ""
echo "🔄 Para reiniciar la aplicación:"
echo "   docker-compose -f compose.yaml restart"
