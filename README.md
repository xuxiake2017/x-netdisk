# x-netdisk
![language java](https://img.shields.io/badge/language-java-green.svg) ![GitHub followers](https://img.shields.io/github/followers/xuxiake2017?label=Follow&style=social) ![GitHub stars](https://img.shields.io/github/stars/xuxiake2017/x-netdisk?style=social) ![GitHub watchers](https://img.shields.io/github/watchers/xuxiake2017/x-netdisk?style=social)  
#### 项目简介
一个分布式在线网盘系统，包含一个Web IM
#### 使用需知
- 整体框架使用springboot，父子模块，前后端分离
- 使用zookeeper做服务注册与发现中心
- 使用shiro做登录认证
- 前端需配合[netdisk-app](https://github.com/xuxiake2017/netdisk-app)、[netdisk-web(待完善)](https://github.com/xuxiake2017/x-netdisk-react-preview)、[netdisk-miniapp](https://github.com/xuxiake2017/netdisk-mp-preview)使用
- 需要nginx做代理
- 需要redis
- 储蓄文件使用了分布式文件服务器FastDFS
- 数据库mysql
- 网盘文件树实现使用了mysql的储存过程
- 确保电脑上安装了mvn，而且加进了系统path中，运行`x-netdisk-web\src\main\resources\lib\install.cmd`，安装`jave-1.0.2.jar`这个lib
- docker部署在windows上遇到`world-writable config file '/etc/mysql/my.cnf' is ignored docker`的问题，把`my.cnf`设置成只读
- 不建议在windows上使用docker，会卡死（自测是这样）
- 记得修改`x-netdisk-web\src\main\resources\application.yml`中的`fdfs-nginx-server`，根据自己的配置来

#### 使用docker部署

1. 安装依赖以及打包
    ```shell
    $ mvn install
    $ mvn package
    ```

2. 构建并启动
    ```shell
    $ docker-compose up --build
    # 或者是
    $ docker compose up --build
    ```

   `web` 镜像会自动安装 LibreOffice、中文字体和 FFmpeg，无需提前构建本地基础镜像。
#### 框架介绍
- chat简易流程图
![chat简易流程图](https://raw.githubusercontent.com/xuxiake2017/x-netdisk/master/pic/chat%E7%AE%80%E6%98%93%E6%B5%81%E7%A8%8B%E5%9B%BE.jpg)
- 文件上传时序图
![文件上传时序图](https://raw.githubusercontent.com/xuxiake2017/x-netdisk/master/pic/%E4%B8%8A%E4%BC%A0%E6%96%87%E4%BB%B6%E6%97%B6%E5%BA%8F%E5%9B%BE.jpeg)
#### 模块介绍
##### `x-netdisk-web`
- 处理web请求，可以部署多个，内部采用redis session集群共享方案
##### `x-netdisk-route`
- 路由集中处理
- 订阅子模块
- 保存、获取用户聊天的路由
- 分发请求
- 可以部署多个（无状态）
##### `x-netdisk-chat-server`(非必须)
- web im 的服务端，可以部署多个
##### `x-netdisk-quartz`
- 使用quartz处理回收站，定时清理用户回收站
- 可以部署多个，quartz自带集群方案
##### `x-netdisk-common`
- 公用mapper、entity、utils，被其他模块所依赖
##### `x-netdisk-admin`
- admin模块，admin后台管理的代码
#### 鸣谢
- [整体框架参考 crossoverJie/cim](https://github.com/crossoverJie/cim)
- [shiro session 共享实现 alexxiyang/shiro-redis](https://github.com/alexxiyang/shiro-redis)
- [FastDFS springboot 解决方案 tobato/FastDFS_Client](https://github.com/tobato/FastDFS_Client)
- [emoji 的 java 转换储存解决方案 vdurmont/emoji-java](https://github.com/vdurmont/emoji-java)
#### 更新日志
##### 2020-03-25
- 增加将socket Ip注册到zookeeper，生产环境socket Ip需要公网
- 上传README.md文件以及数据库脚本
##### 2020-03-26
- 解决项目打包问题
##### 2020-03-31
- 加入quartz定时任务框架，处理回收站
##### 2020-04-11
- 史诗级更新，项目重构，file表拆分(总算把当年犯傻建的表改了...)
##### 2020-05-20
- 增加视频缩略图
- 文件列表增加缩略图字段
##### 2020-06-25
- 增加文档转pdf
##### 2021-03-22
- 判断上传的txt文件编码，如果不是UTF-8无BOM，转码为UTF-8无BOM（解决txt文件转pdf乱码）
##### 2021-05-02
- 增加对小程序一键登录注册的支持以及自动登录

##### 2021-05-02
- 修复上传的图片大于100KB进行缩略；对上传的音频封面图大于100KB进行缩略

##### 2021-07-04
- 增加小程序登出
- 文件列表增加排序方式查询条件

##### 2022-07-29
- 重大更新，增加docker部署

##### 2022-09-15
- 增加系统日志（登录），增加短信发送日志

##### 2023-01-05
- 增加admin模块

##### 持续更新中，如果我的项目能给您带来帮助，请star一下😁
