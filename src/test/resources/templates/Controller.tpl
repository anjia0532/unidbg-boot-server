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
     * @return 结果
     */
    @SneakyThrows @RequestMapping(value = "do-work", method = {RequestMethod.GET, RequestMethod.POST})
    public Object [(${#strings.toLowerCase(ServiceName)})](@RequestBody Object param) {
        // 如果想用body接收参数 就用 @RequestBody 注解
        // 如果想用url传参，就用
        return [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker.doWork(param).get();
    }
}
