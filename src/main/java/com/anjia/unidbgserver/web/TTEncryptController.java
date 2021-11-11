package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.TTEncryptServiceWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 控制类
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
     * 获取ttEncrypt
     * <p>
     * public byte[] ttEncrypt(@RequestParam(required = false) String key1, @RequestBody String body)
     * // 这是接收一个url参数，名为key1,接收一个post或者put请求的body参数
     * key1是选填参数，不写也不报错，值为,body只有在请求方法是POST时才有，GET没有
     *
     * @return 结果
     */
    @SneakyThrows @RequestMapping(value = "encrypt", method = {RequestMethod.GET, RequestMethod.POST})
    public byte[] ttEncrypt() {
        String key1 = "key1";
        String body = "body";
        // 演示传参
        byte[] result = ttEncryptServiceWorker.ttEncrypt(key1, body).get();
        log.info("入参:key1:{},body:{},result:{}", key1, body, result);
        return result;
    }
}
