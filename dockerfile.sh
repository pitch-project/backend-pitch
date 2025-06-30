#!/bin/bash

# Define el contenido del Dockerfile
DOCKERFILE_CONTENT="""# --- ETAPA 1: BUILDER ---
# Usa una imagen de Maven y Java 17 como entorno de construcción.
# Esta imagen es grande e incluye todas las herramientas necesarias para compilar tu proyecto.
FROM maven:3.9.10-amazoncorretto-17 AS builder

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copia los archivos POM de todos los módulos para permitir que Maven descargue las dependencias.
# Esto es crucial para aprovechar el cache de Docker y evitar descargar dependencias en cada build.
COPY ../pom.xml /app/pom.xml
COPY pom.xml /app/pom.xml

# Descarga todas las dependencias del proyecto.
# Esto se hace para que Docker pueda cachear las dependencias,
# acelerando builds posteriores si solo cambia el código fuente.
RUN mvn dependency:go-offline -B

# Copia todo el código fuente del microservicio al contenedor.
COPY src/ /app/src/

# Empaqueta la aplicación como un archivo JAR ejecutable.
RUN mvn package -DskipTests

# --- ETAPA 2: RUNNER (Ejecución) ---
# Usa una imagen base ligera de Java 17 (con JRE, sin herramientas de compilación).
# Esta es la imagen final que usarás en producción.
# openjdk:17-jre-slim es una excelente opción. Si necesitas algo aún más pequeño, puedes usar -alpine.
FROM openjdk:17-jre-slim

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copia el archivo JAR compilado desde la etapa de 'builder'.
# 'builder' es el nombre que le dimos a la primera etapa.
COPY --from=builder /app/target/*.jar /app/app.jar

# Exponer el puerto en el que escucha el microservicio (ej. 8080 por defecto en Spring Boot).
EXPOSE 8080

# Define el comando para ejecutar la aplicación.
ENTRYPOINT [\"java\", \"-jar\", \"/app/app.jar\"]
"

# Lista de directorios de microservicios
MICROSERVICES=("user-service" "payment-service" "customer-service" "audit-service") # Añade o quita según tus microservicios

# Itera sobre cada microservicio y crea el Dockerfile
for SERVICE_DIR in "${MICROSERVICES[@]}"; do
  if [ -d "$SERVICE_DIR" ]; then
    echo "Creando Dockerfile en $SERVICE_DIR/Dockerfile"
    echo "$DOCKERFILE_CONTENT" > "$SERVICE_DIR/Dockerfile"
  else
    echo "Advertencia: El directorio $SERVICE_DIR no existe. Saltando."
  fi
done

echo "Proceso completado."
