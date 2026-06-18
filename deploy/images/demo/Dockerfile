ARG OPENJDK_IMAGE=openjdk:17.0.2-jdk
FROM ${OPENJDK_IMAGE}
WORKDIR /app

COPY start.sh ./
COPY demo-seeder.jar ./

RUN chmod +x start.sh

ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx128m -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["/bin/sh", "/app/start.sh"]
