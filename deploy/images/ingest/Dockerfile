ARG OPENJDK_IMAGE=openjdk:17.0.2-jdk
FROM ${OPENJDK_IMAGE}
WORKDIR /app

COPY start.sh application.yml ./
COPY *.jar ./

RUN chmod +x start.sh

ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1536m -Duser.timezone=Asia/Shanghai"

EXPOSE 4317 4318 8081 18112

ENTRYPOINT ["/bin/sh", "/app/start.sh"]
