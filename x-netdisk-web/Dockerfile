# syntax=docker/dockerfile:1

# Java 8 运行时以及文档、音视频转换工具。
# Ubuntu 22.04 提供带安全更新的 LibreOffice 7.3、FFmpeg 和中文语言包，
# 不再依赖已停止维护的 CentOS 7、RPM Fusion 和第三方压缩包下载地址。
FROM eclipse-temurin:8-jre-jammy AS office-runtime

ARG DEBIAN_FRONTEND=noninteractive

ENV TZ=Asia/Shanghai \
    HOME=/tmp

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        ffmpeg \
        fontconfig \
        fonts-liberation \
        fonts-noto-cjk \
        libreoffice-java-common \
        libreoffice-l10n-zh-cn \
        libreoffice-nogui \
        locales \
        tzdata \
    && locale-gen zh_CN.UTF-8 \
    && ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && fc-cache -f \
    && java -version \
    && libreoffice --headless --version \
    && ffmpeg -version \
    && ffprobe -version \
    && test -x /usr/bin/qt-faststart \
    && rm -rf /var/lib/apt/lists/* /tmp/*

ENV LANG=zh_CN.UTF-8 \
    LANGUAGE=zh_CN:zh \
    LC_ALL=zh_CN.UTF-8

FROM office-runtime AS web

WORKDIR /
RUN mkdir -p /logs

COPY target/x-netdisk-web.jar /x-netdisk-web.jar

VOLUME ["/logs"]
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/x-netdisk-web.jar"]
