package com.anjia.unidbgserver.utils;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * 根据模板引擎生成文件
 *
 * @author AnJia
 * @since 2021-09-13 17:55
 */
public class ThymeleafUtils {
    @SneakyThrows public static void generateByTemplate(String templateName, Map<String, Object> vars, String destFile) {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".tpl");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateEngine.setTemplateResolver(templateResolver);
        StringWriter stringWriter = new StringWriter();
        templateEngine.process(templateName, new Context(Locale.getDefault(), vars), stringWriter);
        FileUtils.writeStringToFile(new File(destFile), stringWriter.toString(), StandardCharsets.UTF_8);
    }
}
