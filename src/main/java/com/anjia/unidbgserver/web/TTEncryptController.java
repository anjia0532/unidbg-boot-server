package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.TTEncryptServiceWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 美团控制类
 *
 * @author AnJia
 * @since 2021-07-26 18:31
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/tt-encrypt", produces = MediaType.APPLICATION_JSON_VALUE)
public class TTEncryptController {

    @Resource(name = "ttEncryptWorker")
    private TTEncryptServiceWorker ttEncryptServiceWorker;

    /**
     * 获取mtgsig
     *
     * @return 结果
     */
    @SneakyThrows @RequestMapping(value = "encrypt", method = {RequestMethod.GET, RequestMethod.POST})
    public byte[] ttEncrypt(@RequestParam(required = false) String key1, @RequestBody String body) {
        log.info("key1是选填参数，不写也不报错，值为:{},body只有在请求方法是POST时才有，GET没有，值为:{}", key1, body);
        return ttEncryptServiceWorker.ttEncrypt(key1, body).get();
    }
}
