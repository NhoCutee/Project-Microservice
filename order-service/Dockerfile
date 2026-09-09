FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy pom.xml TRƯỚC - Docker cache layer này nếu pom.xml không đổi
# thì lần build sau bỏ qua bước download dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (chỉ chạy lại khi pom.xml thay đổi)
RUN ./mvnw dependency:go-offline -B

# Copy source code SAU (layer này thay đổi thường xuyên)
COPY src ./src

# Build jar
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
