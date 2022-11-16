# 基于centos7版本镜像
FROM centos:7
	 
# 以下设置中文语言环境与修改时区
ENV LANG=zh_CN.UTF-8 \
	LANGUAGE=zh_CN:zh \
	LC_ALL=zh_CN.UTF-8
RUN yum install -y glibc-common wget tar \
	&& localedef -c -f UTF-8 -i zh_CN zh_CN.UTF-8 \
	&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
	 
# 加入windows字体包
# ADD chinese.tar.gz /usr/share/fonts/
	 
# 将下载好的包解压到相应文件下
# COPY LibreOffice_7.3.7_Linux_x86-64_rpm.tar.gz /home/
# COPY LibreOffice_7.3.7_Linux_x86-64_rpm_langpack_zh-CN.tar.gz /usr/

RUN cd /home/ \
	&& wget https://mirrors.aliyun.com/tdf/libreoffice/stable/7.3.7/rpm/x86_64/LibreOffice_7.3.7_Linux_x86-64_rpm.tar.gz \
	&& tar zxvf LibreOffice_7.3.7_Linux_x86-64_rpm.tar.gz \
	&& cd /usr/ \
	&& wget https://mirrors.aliyun.com/tdf/libreoffice/stable/7.3.7/rpm/x86_64/LibreOffice_7.3.7_Linux_x86-64_rpm_langpack_zh-CN.tar.gz \
	&& tar zxvf LibreOffice_7.3.7_Linux_x86-64_rpm_langpack_zh-CN.tar.gz \
	&& mkdir -p /usr/share/fonts/ \
	&& cd /usr/share/fonts/ \
	&& wget https://read-1252195440.cos.ap-guangzhou.myqcloud.com/chinese.tar.gz \
	&& tar zxvf chinese.tar.gz
	 
# 执行安装
RUN cd /home/LibreOffice_7.3.7.2_Linux_x86-64_rpm/RPMS/ \
	&& yum localinstall *.rpm -y \
	&& cd /usr/LibreOffice_7.3.7.2_Linux_x86-64_rpm_langpack_zh-CN/RPMS/   \
	&& yum localinstall *.rpm -y \
	&& rm -rf /home/LibreOffice_7.3.7_Linux_x86-64_rpm.tar.gz \
	&& rm -rf /usr/LibreOffice_7.3.7_Linux_x86-64_rpm_langpack_zh-CN.tar.gz \
	&& rm -rf /home/LibreOffice_7.3.7.2_Linux_x86-64_rpm \
	&& rm -rf /usr/LibreOffice_7.3.7.2_Linux_x86-64_rpm_langpack_zh-CN \
	 
	# 安装依赖
	&& yum install ibus -y \
		  
	# 加入中文字体支持并赋权限
	&& cd /usr/share/fonts/ \
	&& chmod -R 755 /usr/share/fonts \
	&& yum install mkfontscale -y \
	&& mkfontscale \
	&& yum install fontconfig -y \
	&& mkfontdir \
	&& fc-cache -fv \
	&& rm -rf /usr/share/fonts/chinese.tar.gz
	 
# 安装java
RUN yum install -y java-1.8.0-openjdk-devel.x86_64

# 安装ffmpeg
RUN yum install -y epel-release \
	&& yum localinstall -y --nogpgcheck https://download1.rpmfusion.org/free/el/rpmfusion-free-release-7.noarch.rpm \
	&& yum install -y ffmpeg ffmpeg-devel \
	#清理缓存,减少镜像大小
	&& yum clean all \
	&& rm -rf /tmp/* rm -rf /var/cache/yum/*

CMD ["bash"]