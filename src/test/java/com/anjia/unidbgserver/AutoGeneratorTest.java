package com.anjia.unidbgserver;

import com.anjia.unidbgserver.utils.ThymeleafUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码自动生成
 *
 * @author AnJia
 * @since 2021-09-13 17:36
 */
@Slf4j
@SpringBootTest
public class AutoGeneratorTest {

    // 这一步生成模板代码
    @Test
    public void testTpl() {
        String serviceName = "MeiTuan";

        Map<String, Object> vars = new HashMap<>();
        vars.put("ServiceName", serviceName);
        vars.put("content", "");

        ThymeleafUtils.generateByTemplate("Service", vars, String.format("src/main/java/com/anjia/unidbgserver/service/%sService.java", serviceName));
        ThymeleafUtils.generateByTemplate("ServiceWorker", vars, String.format("src/main/java/com/anjia/unidbgserver/service/%sServiceWorker.java", serviceName));
        ThymeleafUtils.generateByTemplate("Controller", vars, String.format("src/main/java/com/anjia/unidbgserver/web/%sController.java", serviceName));
        ThymeleafUtils.generateByTemplate("ServiceTest", vars, String.format("src/test/java/com/anjia/unidbgserver/service/%sServiceTest.java", serviceName));

        log.info("linux/mac       : curl http://localhost:9999/api/{}/do-work", serviceName.toLowerCase());
        log.info("windows 浏览器打开: http://localhost:9999/api/{}/do-work", serviceName.toLowerCase());
    }


}
