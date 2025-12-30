FROM eclipse-temurin:21-jdk

COPY . /app
WORKDIR /app

RUN ./mvnw clean package -DskipTests || true

CMD ["java", "-jar", "target/*.jar"]