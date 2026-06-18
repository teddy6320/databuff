ARG OPENJDK_IMAGE=openjdk:17.0.2-jdk
FROM ${OPENJDK_IMAGE}
WORKDIR /app

COPY start.sh application.yml ./
COPY skills/ ./skills/
COPY *.jar ./

RUN chmod +x start.sh \
    && mkdir -p ./data/skills ./data/ai-workspaces

ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx700m -Duser.timezone=Asia/Shanghai"

EXPOSE 27403

ENTRYPOINT ["/bin/sh", "/app/start.sh"]
