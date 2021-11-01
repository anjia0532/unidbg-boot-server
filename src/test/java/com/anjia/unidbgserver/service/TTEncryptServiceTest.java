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
 *
 * @author AnJia
 * @since 2021-08-02 16:31
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TTEncryptServiceTest {

    @Autowired
    TTEncryptServiceWorker ttEncryptServiceWorker;

    @Autowired
    UnidbgProperties properties;

    @SneakyThrows @Test
    void testServiceGetTTEncrypt() {
        TTEncryptService ttEncryptService = new TTEncryptService(properties);
        byte[] data = ttEncryptService.ttEncrypt(null);
        log.info(new String(data));
    }

    @SneakyThrows @Test
    void testWorkerGetTTEncrypt() {
        byte[] data = ttEncryptServiceWorker.ttEncrypt(null, null).get();
        log.info(new String(data));
    }
}
