# reggie_take_out

# 黑马程序员项目 ： 瑞吉外卖

基于SpringBoot的外卖订餐App，分为用户移动端页面和后台管理页面两个部分

完成了教学中的全部内容，并自己实现了教学中没有提到的业务，所有业务功能基本都实现了。另外，短信验证码登录这一块改用了redis实现，同时在前端增加了一些功能，比如在订单查询页面增加了图片显示、后台订单管理显示详情等。最后完善了一些不合理的地方

基本就增删改查，没有复杂业务，虽然有一些坑，但都是可以解决的，适合新手拿来练手。

具体请见：https://blog.csdn.net/qq_44371305/article/details/124458985?spm=1001.2014.3001.5501

# 1、环境

jdk1.8

maven3.6

mysql或者MariaDB

Redis

# 2、技术栈

SpringBoot+MybatisPlus+MariaDB+Redis+vue

# 3、配置

配置文件在reggie_take_out\src\main\resources\application.yml

```yaml
server:
  #端口号
  port: 8080
spring:
  application:
    #应用的名称，可选
    name: reggie_take_out
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      #数据库地址
      url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      #数据库用户名和密码
      username: root
      password: 123456
  redis:
    #redis的地址和端口
    host: localhost
    port: 6379
mybatis-plus:
  configuration:
    #在映射实体或者属性时，将数据库中表名和字段名中的下划线去掉，按照驼峰命名法映射
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID

reggie:
  #图片储存地址
  pic-path: D:\img\
```

# 

