# Usa una imagen base de Eclipse Temurin (OpenJDK oficial actual)
FROM eclipse-temurin:17-jre-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado por Maven
COPY target/*.jar app.jar

# Expone el puerto que usa tu aplicación (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]