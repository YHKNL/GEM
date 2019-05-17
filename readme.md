# 服务器基本配置

以下配置过程需使用 root 用户执行

## 基本配置

### 主机名称

```
echo example.com > /etc/hostname
```

### 语言

语言不正确可能会导致无法正常显示中文

```
echo LANG=en_US.UTF-8 > /etc/locale.conf
```

### 时区

```
ln -sf ../usr/share/zoneinfo/Asia/Shanghai /etc/localtime
```

## 常用软件、软件源

```
yum update -y
yum group install -y development
yum install -y epel-release
yum-config-manager --enable epel
```

若默认的软件源（云服务厂商一般都使用他们自己维护的软件源）中软件版本较旧或速度较慢，可替换为[aliyun](https://opsx.alibaba.com/mirror/centos/)或[清华大学源](https://mirror.tuna.tsinghua.edu.cn/help/centos/)

EPEL 也可改用[中科大源](https://lug.ustc.edu.cn/wiki/mirrors/help/epel)或[清华大学源](https://mirror.tuna.tsinghua.edu.cn/help/epel/)

## 防火墙

````
#有些云服务提供商的centos镜像可能没有默认安装
yum install -y firewalld

# 检查是否正在运行
systemctl status firewalld

# 若未运行，运行并自启动
systemctl start firewalld
systemctl enable firewalld

# 查看默认的zone
firewall-cmd --get-default-zone

# 查看当前使用的zone
firewall-cmd --get-active-zones

# 设置默认使用的zone
firewall-cmd --set-default-zone=dmz

# 允许对外开放的服务。执行 ls /usr/lib/firewalld/services/ 以查看支持的服务列表(mysql,zabbix安装需要暴露80，3306端口，以此为例)
firewall-cmd --zone=public --add-port=80/tcp --permanent    （--permanent永久生效，没有此参数重启后失效）

#关闭80端口
firewall-cmd --zone= public --remove-port=80/tcp --permanent

#查看80端口
firewall-cmd --zone= public --query-port=80/tcp

#以上操作执行完后都需重新载入
firewall -cmd --reload

# 查看所有配置
firewall-cmd --list-all

``````  
__注意：__

若修改了所添加service（如ssh）的端口号，需修改 `/usr/lib/firewalld/services/` 目录下相应配置文件中的端口号，并在修改后执行 `firewall-cmd --reload` 重载配置

 ## SELinux
 [SELinux](https://en.wikipedia.org/wiki/Security-Enhanced_Linux) 是一种安全机制，但容易导致权限问题，在安装ngios/zabbix时为了保险起见关闭

```
# 查看状态
sestatus
```

修改配置文件 `vi /etc/selinux/config`：

```
SELINUX=disabled
```

重启系统以生效(reboot)

重启后还要检查是否关闭(getenforce)如果显示enforcing则没有关闭

## 安装ANMP架构
zabbix/nagios等需运行在这套环境中

安装Apache


````
#使用yum 安装
yum -y install httd*
#查看安装的http包
rpm -qa | grep httpd

启动
systemctl start httpd
查看状态
systemctl status httpd
关闭
systemctl stop httpd
开机启动
systemctl enable httpd
`````
安装成功后会产生下面两个文件夹

```
/etc/httpd/conf/httpd.conf  # 主配置文件
/var/www/html                   # 默认网站家目录
```
__注意：__
修改 http.conf 配置文件有大坑，最好修改之前cp一份，不然格式或者配置错误会导致启动失败，还不知道错在哪。
````
配置文件主要参数
[root@Apache ~]# vim /etc/httpd/conf/httpd.conf
 31 serverRoot "/etc/httpd"           # 存放配置文件的目录
 42 Listen 80           # Apache服务监听端口
 66 User apache     # 子进程的用户
 67 Group apache   # 子进程的组
 86 ServerAdmin root@localhost  # 设置管理员邮件地址
119 DocumentRoot "/var/www/html" --网站家目录
# 设置DocumentRoot指定目录的属性
131 <Directory "/var/www/html">   # 网站容器开始标识
144 Options Indexes FollowSymLinks   # 找不到主页时，以目录的方式呈现，并允许链接到网站根目录以外
151 AllowOverride None                # none不使用.htaccess控制,all允许
156 Require all granted                 # granted表示运行所有访问，denied表示拒绝所有访问
157 </Directory>    # 容器结束
164 DirectoryIndex index.html       # 定义主页文件，当访问到网站目录时如果有定义的主页文件，网站会自动访问
316 AddDefaultCharset UTF-8      # 字符编码，如果中文的话，有可能需要改为gb2312或者gbk,因你的网站文件的默认编码而异

``````
详情参考https://blog.51cto.com/13525470/2070375

安装Nginx
nginx前需要下载安装几个环境
```
#gcc安装
yum install gcc-c++

#PCRE pcre-devel 安装
yum install -y pcre pcre-devel

#zlib 安装
yum install -y zlib zlib-devel

#OpenSSL 安装
yum install -y openssl openssl-devel

```````
安装
```
wget -c https://nginx.org/download/nginx-1.10.1.tar.gz

#解压
tar -zxvf nginx-1.10.1.tar.gz
cd nginx-1.10.1

#配置(推荐默认)

 ./configure

 #编译安装make&make isntall
 #查找安装路径
 whereis nginx
  ````
启动停止(默认路径)
````
cd /usr/local/nginx/sbin/
./nginx 
./nginx -s stop
./nginx -s quit
./nginx -s reload
``````
## MARIADB

MariaDB 是MariaDB 是 MySQL 的主要分支之一，推荐替代 MySQL 使用

 安装
 `````
 1,yum -yinstall mariadb mariadb-server
 2.systemctl start mariadb
 3.systemctl enable mariadb
 4.mysql_secure_installation(在是否开启远程哪里要注意，其他根据需求选择）
 5.登陆 mysql-uroot -p

 `````
 
 配置
 
``````
vi /etc/my.cnf
init_connect='SET collation_connection = utf8_unicode_ci' 
init_connect='SET NAMES utf8' 
character-set-server=utf8 
collation-server=utf8_unicode_ci 
skip-character-set-client-handshake

vi /etc/my.cnf.d/client.cnf

在client.cnf文件中[client]下添加：
default-character-set=utf8


vi /etc/my.cnf.d/mysql-clients.cnf

在mysql-clients.cnf文件中[mysql]下添加：
default-character-set=utf8
``````````````


 重启
  
 ```````````
 #systemctl restart mariadb

 #mysql -u root -p
 
 查看字符集
 MariaDB [(none)]> show variables like "%character%";show variables like "%collation%";
 
 ```````````````

## PHP
 
 安装
 
 通过yum安装更新yum源
 `````
 #rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
 #rpm -Uvh https://mirror.webtatic.com/yum/el7/webtatic-release.rpm

 #查询安装
 #yum search php71w

 yum install php71w php71w-cli php71w-common php71w-devel php71w-embedded php71w-fpm php71w-gd php71w-mbstring php71w-mysqlnd php71w-  opcache php71w-pdo php71w-xml

 ``````

## 安装zabbix

通过yum安装
```````````````````
rpm -ivh http://repo.zabbix.com/zabbix/3.4/rhel/7/x86_64/zabbix-release-3.4-2.el7.noarch.rpm
yum install -y zabbix-server-mysql zabbix-get zabbix-web zabbix-web-mysql zabbix-agent zabbix-sender
#登陆数据库执行创建账户授权
create database zabbix character set utf8 collate utf8_bin;
show databases;
grant all privileges on zabbix.* to zabbix@localhost identified by 'zabbix';
#刷新
flush privileges;
exit

#导入表(这里有坑zabbix-server-mysql-3.2.10，注意版号)

cd /usr/share/doc/zabbix-server-mysql-3.2.10/
gunzip create.sql.gz

#再次登陆MySQL use zabbix

soruce create.sql

#配置zabbix_server.conf(配错会导致启动失败)
cd /etc/zabbix

DBHost=localhost
DBName=zabbix
DBUser=zabbix
DBPassword=zabbix
DBSocket=/var/lib/mysql/mysql.sock

#运行zabbix_server

systemctl start zabbix-server.service
systemctl enable zabbix-server.service

#配置php

cd /etc/httpd/conf.d

配置时间

vi zabbix.conf

重启httpd

`````````````````````````

## 关于VM桥接外网

win10在防火墙重新制定入墙规则。开放80和9666,3306端口。
VM在虚拟机网路编辑设置中设置NAT模式。
详情参考https://blog.csdn.net/zk673820543/article/details/50548084







 

  
 
 
 
 


 
 
 


  
 
 
 
 
 



 

