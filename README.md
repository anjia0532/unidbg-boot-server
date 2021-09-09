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

### 环境准备

1. 必须使用Java8
2. Maven3.5以上 ，如果电脑没有安装Maven，最简单办法是将下面的 `mvn` 命令替换成 `mvnw` ，会自动下载maven 

### 使用定制化/快照版unidbg

```bash
git clone https://github.com/zhkl0228/unidbg.git
cd unidbg
mvn clean install -Dgpg.skip=true -T10
```

以最新快照版 `0.9.5-SNAPSHOT` 为例，修改 `unidbg-boot-server/pom.xml` 里的 `<unidbg.version>0.9.4</unidbg.version>`
为 `<unidbg.version>0.9.5-SNAPSHOT</unidbg.version>`

后续java打包或者docker不变

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

## 快速体验

```bash
# 体验jar版本
mvn package -T10 -DskipTests
java -jar target\unidbg-boot-server-0.0.1-SNAPSHOT.jar

# 体验docker版本
docker run --restart=always -d -p9999:9999 anjia0532/unidbg-boot-server 
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

瓶颈在cpu上,demo内存基本在400-600M左右，不会随着并发高而暴涨(注意，仅是此demo情况下，具体还是看实际业务复杂度)

![](docs/1.png)

## 常见问题

### 访问demo返回乱码

正常，因为demo返回的是 `byte[]` 数组，没有对其进行处理 ![](docs/2.png)

### 如何进行单元测试

传统的办法是在一个大类里写各种main函数，每次改了都注释，或者写多个main函数，来回改名字。其实不用这么麻烦，用单元测试就可以了。

具体可以参考  `src/test/java/com/anjia/unidbgserver/service`  下的

- `TTEncryptTest.java` 基本等同于main启动，不加载spring那一套，启动速度快，不测试worker，只测试实际业务

- `TTEncryptWorkerTest` 加载spring整套环境，测试多线程worker

`@Test` 可以简单理解成一个个mian函数，可以直接运行的

`@BeforeEach` 是不管起哪个 `@Test` 方法都会先执行 带`@BeforeEach`

### 修改日志等级

正常运行的，修改 `src/main/resources/logback-spring.xml` 里的

单元测试的，修改 `src/test/resources/logback-test.xml` 里的

### 服务挂掉后如何自动重启

群里有朋友反馈服务运行后会自动挂掉，我没有遇到也没有复现，但是针对这个问题，可以换个思路，将保证服务不挂掉，改成，即使服务挂掉，如何快速自动重启

基于此，给出跨平台的几种方案

跨平台的方案使用docker,支持windows/mac/linux (如何安装docker 参考docker官方文档 https://docs.docker.com/engine/install/)

```bash
docker run --restart=always -d -p9999:9999 anjia0532/unidbg-boot-server
```

windows 下可以用 [nssm](http://www.nssm.cc/download),
参考 [nssm 在windows上部署服务](https://www.cnblogs.com/hai-cheng/p/8670395.html)

linux和mac os 下可以用 Supervisor 参考 [Supervisor-java守护进程工具](https://blog.csdn.net/fuchen91/article/details/107086802/)

### 打包后无法访问文件，报 java.io.FileNotFoundException

以demo里的为例 https://github.com/anjia0532/unidbg-boot-server/blob/main/src/main/java/com/anjia/unidbgserver/service/TTEncrypt.java#L61

把二进制文件 libttEncrypt.so 放到 `src/resources/data/apks/so/` 下, 然后调用`TempFileUtils.getTempFile(LIBTT_ENCRYPT_LIB_PATH)` (
将classpath下的文件copy到临时目录里，后边访问临时目录的即可。)

**为啥不直接写死绝对路径，一旦换机器部署，就要重新修改源代码，重新打包，尤其是把代码分发给网友时，很容易踩坑报错。**

```java

        private final static String LIBTT_ENCRYPT_LIB_PATH="data/apks/so/libttEncrypt.so";

        vm.loadLibrary(TempFileUtils.getTempFile(LIBTT_ENCRYPT_LIB_PATH),false);
```

### 高并发请求

参考 `com.anjia.unidbgserver.web.TTEncryptController` 和 `com.anjia.unidbgserver.service.TTEncrypt`
和 `com.anjia.unidbgserver.service.TTEncryptWorker` 和 `com.anjia.unidbgserver.service.TTEncryptWorkerTest`

主要unidbg模拟逻辑在 `com.anjia.unidbgserver.service.TTEncrypt` 里

`com.anjia.unidbgserver.web.TTEncryptController` 是暴露给外部http调用的

`com.anjia.unidbgserver.service.TTEncryptWorker` 是用多线程包装了一层

`com.anjia.unidbgserver.service.TTEncryptWorkerTest` 是单元测试

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
