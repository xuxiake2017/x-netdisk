# 指定镜像
FROM openjdk:8-jdk-alpine
# 修改apk中央仓库地址
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 安装依赖包（为了生成验证码）
RUN apk add --update ttf-dejavu fontconfig
# 修改时区
RUN apk add --update tzdata
ENV TZ=Asia/Shanghai
# 删除apk安装依赖包的缓存
RUN rm -rf /var/cache/apk/*
# 申明要挂载的目录
VOLUME /logs
# 复制jar包到容器中
ADD ./target/x-netdisk-admin.jar /x-netdisk-admin.jar
# 申明要暴露的端口
EXPOSE 8300
# 执行命令
ENTRYPOINT ["java","-jar","/x-netdisk-admin.jar"]
