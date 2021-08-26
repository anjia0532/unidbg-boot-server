package com.anjia.unidbgserver.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单院测试
 *
 * @author AnJia
 * @since 2021-08-02 16:31
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TTEncryptTest {

    @Autowired
    TTEncryptWorker ttEncryptWorker;

    @SneakyThrows @Test
    void getMtgsig() {
        byte[] data = ttEncryptWorker.ttEncrypt().get();
        log.info(new String(data));
    }
}