FROM openjdk:8
LABEL authors="CKT"

# 设置工作目录
WORKDIR /app

# 复制项目的 jar 包和配置文件到容器中
COPY target/*.jar /app/app.jar
COPY src/main/resources/application.yaml /app/config/application.yaml
COPY src/main/resources/map.csv /app/config/map.csv

# 设置时区（可选）
ENV TZ=Asia/Shanghai
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]