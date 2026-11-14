# Primeiro estagio: o Maven compila o projeto.
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Segundo estagio: somente Java e o jar gerado.
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=8080
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/climacidadao-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
