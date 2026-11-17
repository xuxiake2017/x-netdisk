# Java 8 运行时
FROM eclipse-temurin:8-jre-jammy

ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Shanghai

# 验证码字体和时区数据
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core tzdata \
    && ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && fc-cache -f \
    && java -version \
    && rm -rf /var/lib/apt/lists/*

VOLUME ["/logs"]
COPY target/x-netdisk-admin.jar /x-netdisk-admin.jar
EXPOSE 8300
ENTRYPOINT ["java", "-jar", "/x-netdisk-admin.jar"]
