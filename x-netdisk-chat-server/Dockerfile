# Java 8 运行时
FROM eclipse-temurin:8-jre-jammy

ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata \
    && ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && java -version \
    && rm -rf /var/lib/apt/lists/*

VOLUME ["/logs"]
COPY target/x-netdisk-chat-server.jar /x-netdisk-chat-server.jar
EXPOSE 8100 9527
ENTRYPOINT ["java", "-jar", "/x-netdisk-chat-server.jar"]
