package com.anjia.unidbgserver.utils;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * JniTraceDTO
 *
 * @author AnJia
 * @since 2021-09-11 12:13
 */
@Data
public class JniTraceDTO {

    /**
     * 线程id
     */
    private String threadId;
    /**
     * 调用时间
     */
    private String time;
    /**
     * jni方法名
     */
    private String jniMethodName;

    /**
     * jni参数
     */
    private Map<String, Object> args = new HashMap<>(10);

    /**
     * 库描述信息 包括地址啥的
     */
    private String libDesc;

    /**
     * jni 函数返回值
     */
    private String jniRetVal;


}
