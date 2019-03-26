FROM openjdk:8-jre-slim
COPY . /app
WORKDIR /app
ENTRYPOINT /app/bin/datapuppy
