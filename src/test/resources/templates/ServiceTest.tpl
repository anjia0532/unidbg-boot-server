package com.anjia.unidbgserver.service;


import com.anjia.unidbgserver.config.UnidbgProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单元测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class [[${ServiceName}]]ServiceTest {
    @Autowired
    UnidbgProperties properties;

    /**
     * 业务逻辑
     */
    [(${ServiceName})]Service [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service;

    /**
     * 多线程
     */
    @Autowired
    [(${ServiceName})]ServiceWorker [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker;

    @SneakyThrows @Test
    void test[(${ServiceName})]Service() {
        // 调用测试方法
        [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service = new [(${ServiceName})]Service(properties);
        log.info("{}测试,结果:{}","[(${ServiceName})]",[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service.doWork(null));
    }

    @SneakyThrows @Test
    void test[(${ServiceName})]ServiceWorker() {
        // 调用测试方法
        log.info("{}测试,结果:{}","[(${ServiceName})]",[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker.doWork(null).get());
    }
}