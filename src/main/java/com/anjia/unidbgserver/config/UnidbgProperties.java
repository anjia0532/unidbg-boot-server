package com.anjia.unidbgserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * unidbg配置类
 *
 * @author AnJia
 * @since 2021-07-26 19:13
 */
@Data
@ConfigurationProperties(prefix = "application.unidbg")
public class UnidbgProperties {
    /**
     * 是否使用 DynarmicFactory
     */
    boolean dynarmic;
    /**
     * 是否打印调用信息
     */
    boolean verbose;

    /**
     * 是否使用异步多线程
     */
    boolean async = true;
}
