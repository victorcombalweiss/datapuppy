FROM openjdk:8-jre-slim
COPY . /app
WORKDIR /app
EXPOSE 8080
ENTRYPOINT /app/bin/datapuppy
