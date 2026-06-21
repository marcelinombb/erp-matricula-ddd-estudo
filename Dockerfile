# =============================================================================
# ERP Matrícula — Dockerfile Multi-Stage
# =============================================================================
# Build sem dependências no host: apenas Docker é necessário.
# Para construir e executar: docker compose up
# =============================================================================

# =============================================================================
# Stage 1: Compilação — JDK completo para compilar com Maven
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Maven instalado no container (mvnw não está no repositório)
# alpine usa apk em vez de apt-get; --no-cache evita camada de índice de pacotes
RUN apk add --no-cache maven

# Copiar o projeto inteiro para o contexto de build
COPY . .

# Compilar apenas o módulo da aplicação, ignorando testes
# -DskipTests: testes automatizados são v2 (TEST-01..03 no REQUIREMENTS.md)
# -f aponta diretamente para o pom.xml do módulo (sem pom pai na raiz)
# JAR gerado em: erp-matricula-app/target/erp-matricula-0.1.0-SNAPSHOT.jar
RUN mvn -q package -DskipTests -f erp-matricula-app/pom.xml

# =============================================================================
# Stage 2: Runtime — apenas JRE (menor que JDK) + o JAR compilado
# =============================================================================
# A imagem final não contém Maven, JDK ou código-fonte — apenas o JAR executável
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar apenas o JAR do stage anterior — resultado: imagem final ~200MB (sem Maven/JDK)
COPY --from=builder /build/erp-matricula-app/target/erp-matricula-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
