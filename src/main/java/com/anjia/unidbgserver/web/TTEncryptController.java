package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.TTEncryptWorker;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 美团控制类
 *
 * @author AnJia
 * @since 2021-07-26 18:31
 */
@RestController
@RequestMapping(path = "/api/tt-encrypt", produces = MediaType.APPLICATION_JSON_VALUE)
public class TTEncryptController {

    @Resource(name = "ttEncryptWorker")
    private TTEncryptWorker ttEncryptWorker;

    /**
     * 获取mtgsig
     *
     * @return 结果
     */
    @SneakyThrows @RequestMapping(value = "encrypt", method = {RequestMethod.GET, RequestMethod.POST})
    public byte[] ttEncrypt() {
        return ttEncryptWorker.ttEncrypt().get();
    }
}
