# ETAPA 1: BUILD (La construcción)
# Esta imagen tiene Maven y JDK para compilar el proyecto. Luego, copiamos el código fuente, construimos el JAR y lo preparamos para la siguiente etapa.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ETAPA 2: RUNTIME (El producto final)
# Esta imagen es más ligera, solo con el JRE necesario para ejecutar la aplicación. Copiamos el JAR construido en la etapa anterior y lo preparamos para ejecutarse.
# El la imagen de este contener solamente contiene el JRE necesario para ejecutar la aplicación, lo que la hace más ligera y eficiente. Al copiar solo el JAR construido en la etapa anterior, aseguramos que la imagen final sea lo más pequeña posible, sin incluir herramientas de construcción o dependencias innecesarias. Esto mejora el rendimiento y reduce el tiempo de despliegue en entornos de producción.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar product.jar
ENTRYPOINT ["java", "-jar", "product.jar"]