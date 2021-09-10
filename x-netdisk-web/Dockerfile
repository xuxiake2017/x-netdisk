# 指定镜像
FROM xu/libreoffice
# 申明要挂载的目录
VOLUME /logs
# 复制jar包到容器中
ADD ./target/x-netdisk-web.jar /x-netdisk-web.jar
# 申明要暴露的端口
EXPOSE 8080
# 执行命令
ENTRYPOINT ["java","-jar","/x-netdisk-web.jar"]
