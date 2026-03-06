# ESTÁGIO 1: Compilação (O Docker faz o "mvn clean package" por você)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copia o pom.xml e o código fonte para dentro do container
COPY pom.xml .
COPY src ./src

# Executa o build dentro do container (ignora o mvn local)
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Cria a imagem final leve)
# Utilizamos o JRE (apenas o necessário para rodar) na versão jammy (Ubuntu)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copia o .jar gerado no Estágio 1 para o Estágio 2
COPY --from=build /app/target/*.jar app.jar

# Copia as credenciais do Google para a pasta de recursos no container
COPY src/main/resources/crendecial.json /app/crendecial.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]