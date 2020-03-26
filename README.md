# x-netdisk
![](https://img.shields.io/badge/language-java-green.svg) ![GitHub followers](https://img.shields.io/github/followers/xuxiake2017?label=Follow&style=social) ![GitHub stars](https://img.shields.io/github/stars/xuxiake2017/x-netdisk?style=social) ![GitHub watchers](https://img.shields.io/github/watchers/xuxiake2017/x-netdisk?style=social)  
#### 项目简介
一个分布式在线网盘系统，包含一个Web IM
#### 使用需知
- 整体框架使用springboot，父子模块，前后端分离
- 使用zookeeper做服务注册与发现中心
- 使用shiro做登录认证
- 前端需配合[netdisk-app](https://github.com/xuxiake2017/netdisk-app)使用
- 需要nginx做代理
- 需要redis
- 储蓄文件使用了分布式文件服务器FastDFS
- 数据库mysql
- 网盘文件树实现使用了mysql的储存过程
#### 框架介绍（待补充）
#### 模块介绍（待补充）
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
##### 持续更新中，如果我的项目能给您带来帮助，请star一下😁