package com.anjia.unidbgserver.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * 将classpath下的文件copy到临时目录下
 *
 * @author AnJia
 * @since 2021-09-07 17:58
 */
public class TempFileUtils {
    public static File getTempFile(String classPathFileName) throws IOException {
        File soLibFile = new File(System.getProperty("java.io.tmpdir"), classPathFileName);
        if (!soLibFile.exists()) {
            FileUtils.copyInputStreamToFile(new ClassPathResource(classPathFileName).getInputStream(), soLibFile);
        }
        return soLibFile;
    }
}
