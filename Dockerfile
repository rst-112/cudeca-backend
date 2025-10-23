# Usa una imagen base oficial de Java 21
FROM eclipse-temurin:21-jdk

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos de Maven Wrapper y pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Da permisos al mvnw
RUN chmod +x mvnw

# Descarga dependencias para cachearlas
RUN ./mvnw dependency:go-offline

# Copia el resto del proyecto
COPY src src

# Compila y empaqueta el proyecto (sin tests)
RUN ./mvnw clean package -DskipTests

# Expone el puerto (Render usará uno dinámico)
EXPOSE 8080

# Define el comando de arranque
CMD ["java", "-jar", "target/cudeca-backend-0.0.1-SNAPSHOT.jar"]
