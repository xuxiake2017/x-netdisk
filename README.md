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
#### 框架介绍
- chat简易流程图
![chat简易流程图](https://raw.githubusercontent.com/xuxiake2017/x-netdisk/master/pic/chat%E7%AE%80%E6%98%93%E6%B5%81%E7%A8%8B%E5%9B%BE.jpg)
- 文件上传时序图
![文件上传时序图](https://raw.githubusercontent.com/xuxiake2017/x-netdisk/master/pic/%E4%B8%8A%E4%BC%A0%E6%96%87%E4%BB%B6%E6%97%B6%E5%BA%8F%E5%9B%BE.jpeg)
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
##### 2020-03-31
- 加入quartz定时任务框架，处理回收站
##### 2020-04-11
- 史诗级更新，项目重构，file表拆分(总算把当年犯傻建的表改了...)
##### 2020-05-20
- 增加视频速率图
- 文件列表增加缩率图字段
##### 持续更新中，如果我的项目能给您带来帮助，请star一下😁