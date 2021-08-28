# 基于unidbg0.9.4和spring boot 2.5.3开发的高并发server服务器

## application.yml 讲解

```yaml
server:
  # 端口
  port: 9999

application:
  unidbg:
    # 是否启用 dynarmic 引擎
    dynarmic: false
    # 是否打印jni调用细节 vm.setVerbose()
    verbose: false

# 多线程相关
spring:
  task:
    execution:
      pool:
        allow-core-thread-timeout: true
        # 8个核心线程
        core-size: 8
        # 超过多久没用的线程自动释放
        keep-alive: 60s
        # 最多增长到多少线程
        max-size: 8
```

## 使用

### java 打包

```
# 打包
mvn package -T10 -DskipTests
# 运行
java -jar target\unidbg-boot-server-0.0.1-SNAPSHOT.jar
```

### docker打包

用docker打包是为了避免个人电脑和生产服务器环境不一致导致的启动失败或者各种问题，保证了开发和生产环境的一致性，以及快速安装等需求

如何安装docker 参考docker官方文档 https://docs.docker.com/engine/install/
**注意**

- 如果是windows的powershell, - 需要改成 `- ,建议windows用cmd
- 将 your_docker_hub_username 换成真实的用户名 ,将 your_docker_hub_password 换成真实的密码
-

```bash

# 方案1 打包并发布到docker hub
mvn compile -Djib.to.auth.username=your_docker_hub_username  -Djib.to.auth.password=your_docker_hub_password -Djib.to.image=your_docker_hub_username/unidbg-boot-server  jib:build -Dmaven.test.skip=true --batch-mode -T4

# 方案2 直接打到docker 守护进程里
mvn compile  -Djib.to.image=your_docker_hub_password/unidbg-boot-server  jib:dockerBuild -Dmaven.test.skip=true --batch-mode -T4

# 方案3 打成docker.tar二进制包
mvn compile  -Djib.to.image=your_docker_hub_password/unidbg-boot-server  jib:buildTar -Dmaven.test.skip=true --batch-mode -T4
docker load --input target/jib-image.tar

# 在装有docker的机器上运行
sudo docker run  -d -p9999:9999 your_docker_hub_password/unidbg-boot-server 

```

## 调用

```bash
curl  http://127.0.0.1:9999/api/tt-encrypt/encrypt
```

## 压测

在我个人开发电脑上，压测结果是每秒4003.10次(QPS 4003.10)

```
[root@wrk]# docker run --rm  williamyeh/wrk -t12 -c400 -d30s http://127.0.0.1:9999/api/tt-encrypt/encrypt
Running 30s test @ http://127.0.0.1:9999/api/tt-encrypt/encrypt
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   105.55ms   68.17ms 982.93ms   94.97%
    Req/Sec   341.43     55.05   460.00     80.70%
  120432 requests in 30.08s, 14.72MB read
  Socket errors: connect 0, read 0, write 81, timeout 0
Requests/sec:   4003.10
Transfer/sec:    501.09KB
```

瓶颈在cpu上

![](docs/1.png)

## 常见问题

### 高并发请求

参考 `com.anjia.unidbgserver.web.TTEncryptController` 和 `com.anjia.unidbgserver.service.TTEncrypt`
和 `com.anjia.unidbgserver.service.TTEncryptWorker` 和 `com.anjia.unidbgserver.service.TTEncryptTest`

主要unidbg模拟逻辑在 `com.anjia.unidbgserver.service.TTEncrypt` 里

`com.anjia.unidbgserver.web.TTEncryptController` 是暴露给外部http调用的

`com.anjia.unidbgserver.service.TTEncryptWorker` 是用多线程包装了一层

`com.anjia.unidbgserver.service.TTEncryptTest` 是单元测试

### 修改日志等级

修改 `logback-spring.xml`

例如 `<logger name="com.github.unidbg" level="WARN"/>`,意味着 `com.github.unidbg` 包 及该包名空间以下的只打印`WARN`和`ERROR`

### 升级spring boot或者unidbg版本

修改`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- 忽略其他部分 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <!-- spring boot版本号 -->
        <version>2.5.3</version>
        <relativePath/>
    </parent>
    <!-- 忽略其他部分 -->
    <properties>
        <!-- 改成unidbg的版本号 -->
        <unidbg.version>0.9.4</unidbg.version>
        <!-- 忽略其他部分 -->
    </properties>
    <!-- 忽略其他部分 -->
</project>
```