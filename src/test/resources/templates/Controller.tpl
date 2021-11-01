package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.[(${ServiceName})]ServiceWorker;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * MVC控制类
 *
 * @author AnJia
 */
@RestController
@RequestMapping(path = "/api/[(${#strings.toLowerCase(ServiceName)})]", produces = MediaType.APPLICATION_JSON_VALUE)
public class [(${ServiceName})]Controller {

    @Resource(name = "[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker")
    private [(${ServiceName})]ServiceWorker [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker;

    /**
     * 获取 [(${#strings.toLowerCase(ServiceName)})] 计算结果
     *
     * public byte[] ttEncrypt(@RequestParam(required = false) String key1, @RequestBody String body)
     * 这是接收一个url参数，名为key1,接收一个post或者put请求的body参数
     * key1是选填参数，不写也不报错，值为,body只有在请求方法是POST时才有，GET没有
     *
     * @return 结果
     */

    @SneakyThrows @RequestMapping(value = "do-work", method = {RequestMethod.GET, RequestMethod.POST})
    public Object [(${#strings.toLowerCase(ServiceName)})]() {
        String key1="key1";
        String body="body";
        // 演示传参
        return [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker.doWork(body).get();
    }
}
