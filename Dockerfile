# ESTÁGIO 1: Build (Compilação)
# Usamos a versão 3.9 que é mais estável para o Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia apenas o pom.xml primeiro para aproveitar o cache do Docker (agiliza o build)
COPY pom.xml .
RUN mvn dependency:go-offline

# Agora copia o código e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Imagem leve)
# CORREÇÃO: Mudado de 17 para 21 para bater com o estágio de build
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

# Copia as credenciais do Google para a raiz da aplicação no container
# O seu código Java deve buscar o arquivo em "/app/crendecial.json" ou via classpath
COPY src/main/resources/crendecial.json /app/crendecial.json

EXPOSE 8080

# Adicionamos um parâmetro para ajudar a identificar erros de fuso horário, comum em agendamentos
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]