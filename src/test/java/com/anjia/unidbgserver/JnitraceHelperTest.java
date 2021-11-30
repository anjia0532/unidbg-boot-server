package com.anjia.unidbgserver;

import com.anjia.unidbgserver.utils.JniTraceDTO;
import com.anjia.unidbgserver.utils.ThymeleafUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 基于jnitrace log自动生成unidbg辅助补环境相关代码
 *
 * @author AnJia
 * @since 2021-09-13 17:36
 */
@Slf4j
public class JnitraceHelperTest {

    /**
     * 这一步生成模板代码
     */
    @Test
    public void testTpl() throws DecoderException, IOException {
        // jnitrace log 文件 路径
        String mt = "meituan.txt";
        // 名字
        String serviceName = "MeiTuan";

        Map<String, Object> vars = new HashMap<>();
        vars.put("ServiceName", serviceName);
        vars.put("content", generatorCode(mt));

        // 不需要的可以注释掉
        // Service
        ThymeleafUtils.generateByTemplate("Service", vars, String.format("src/main/java/com/anjia/unidbgserver/service/%sService.java", serviceName));
        // ServiceWorker
        ThymeleafUtils.generateByTemplate("ServiceWorker", vars, String.format("src/main/java/com/anjia/unidbgserver/service/%sServiceWorker.java", serviceName));
        // Controller
        ThymeleafUtils.generateByTemplate("Controller", vars, String.format("src/main/java/com/anjia/unidbgserver/web/%sController.java", serviceName));
        // ServiceTest
        ThymeleafUtils.generateByTemplate("ServiceTest", vars, String.format("src/test/java/com/anjia/unidbgserver/service/%sServiceTest.java", serviceName));

        // 可以在本类搜 answer 变量，将部分解析，基于签名直接返回预设的结果，不用走jnitrace的解析
    }

    // 目前实现的类
    Set<String> COMMON_METHOD = new HashSet<String>() {{
        add("JNIEnv->CallStaticObjectMethodV");
        add("JNIEnv->CallStaticLongMethodV");
        add("JNIEnv->GetIntField");
        add("JNIEnv->GetStaticObjectField");
        add("JNIEnv->GetObjectField");
        add("JNIEnv->CallBooleanMethodV");
        add("JNIEnv->NewObjectV");
        add("JNIEnv->CallObjectMethodV");

        add("JNIEnv->CallIntMethod");
        add("JNIEnv->CallObjectMethod");
        add("JNIEnv->CallStaticVoidMethodV");
        add("JNIEnv->CallStaticBooleanMethod");
    }};

    @Test
    @SneakyThrows
    public String generatorCode(String jnitraceLogFilePath) throws DecoderException, IOException {

        StringBuilder builder = new StringBuilder();
        List<JniTraceDTO> traces = parseLogs(new File(jnitraceLogFilePath));
        Map<String, List<JniTraceDTO>> methodTraces = StreamEx.of(traces).groupingBy(JniTraceDTO::getJniMethodName);
        String method;
        Method[] methods = JnitraceHelperTest.class.getDeclaredMethods();
        Map<String, Method> methodsMap = new HashMap<>(methods.length);

        for (Method m : methods) {
            if (m.getName().startsWith("print")) {
                methodsMap.put(m.getName(), m);
            }
        }

        for (Map.Entry<String, List<JniTraceDTO>> entry : methodTraces.entrySet()) {
            method = StringUtils.substringAfter(entry.getKey(), "->");
            Method m = methodsMap.get("print" + method);
            if (Objects.isNull(m)) {
                log.warn("{} not support", entry.getKey());
                continue;
            }

            if (m.getGenericParameterTypes().length == 2) {
                builder.append(m.invoke(this, entry.getValue(), traces));
            } else if (m.getGenericParameterTypes().length == 1) {
                builder.append( m.invoke(this, entry.getValue()));
            }
        }
        //builder.append(printCallStaticObjectMethodV(methodTraces.get("JNIEnv->CallStaticObjectMethodV")));
        //builder.append(printCallStaticLongMethodV(methodTraces.get("JNIEnv->CallStaticLongMethodV")));
        //builder.append(printGetIntField(methodTraces.get("JNIEnv->GetIntField"), traces));
        //builder.append(printGetStaticObjectField(methodTraces.get("JNIEnv->GetStaticObjectField"), traces));
        //builder.append(printGetObjectField(methodTraces.get("JNIEnv->GetObjectField"), traces));
        //builder.append(printCallBooleanMethodV(methodTraces.get("JNIEnv->CallBooleanMethodV")));
        //builder.append(printNewObjectV(methodTraces.get("JNIEnv->NewObjectV")));
        //builder.append(printCallObjectMethodV(methodTraces.get("JNIEnv->CallObjectMethodV")));

        return builder.toString();
    }

    private String printCallStaticObjectMethodV(List<JniTraceDTO> traces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<String, String>() {{
            put("java/util/UUID->randomUUID()Ljava/util/UUID;", "vm.resolveClass(\"java/util/UUID\").newObject(UUID.randomUUID())");
            put("java/lang/Long->valueOf(J)Ljava/lang/Long;", "DvmLong.valueOf(vm, vaList.getLongArg(0))");
            put("java/lang/String->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", "new StringObject(vm, String.format(vaList.getObjectArg(0).getValue().toString(), vaList.getIntArg(1)))");
        }};
        traces = StreamEx.of(traces).sortedBy(trace -> trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jmethodID")).toList();
        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {\n");
        builder.append("        printArgs(vm,\"callStaticObjectMethodV\", signature, vaList);\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jmethodID");
            if (method.startsWith("0x")) {
                continue;
            }
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method)
                    .append(",参数:").append(trace.getArgs().get("va_list"))
                    .append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime()).append(",参数:").append(trace.getArgs().get("va_list"))
                .append(",样例:").append(trace.getJniRetVal()).append("\n");
            if (answer.containsKey(method)) {
                builder.append("                return ").append(answer.get(method)).append(";\n");
            } else {
                builder.append("                return new StringObject(vm, \"").append(trace.getJniRetVal()).append("\");\n");
            }
        }
        builder.append("\n            default:\n");
        builder.append("                return super.callStaticObjectMethod(vm, dvmClass, signature, vaList);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printCallObjectMethodV(List<JniTraceDTO> traces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<String, String>() {{
            put("java/util/UUID->toString()Ljava/lang/String;", "new StringObject(vm, dvmObject.getValue().toString())");
            put("android/content/pm/PackageManager->getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;", "new ApplicationInfo(vm)");
            put("java/lang/ClassLoader->loadClass(Ljava/lang/String;)Ljava/lang/Class;", "dvmObject.getObjectType()");
        }};

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {\n");
        builder.append("        printArgs(vm,\"callObjectMethodV\", signature, vaList);\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jobject") + "->" + trace.getArgs().get("jmethodID");
            if (method.startsWith("0x")) {
                continue;
            }
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method)
                    .append(",参数:").append(trace.getArgs().get("va_list"))
                    .append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime())
                .append(",参数:").append(trace.getArgs().get("va_list"))
                .append(",样例:").append(trace.getJniRetVal()).append("\n");
            if (answer.containsKey(method)) {
                builder.append("                return ").append(answer.get(method)).append(";\n");
            } else {
                builder.append("                return new StringObject(vm, \"").append(trace.getJniRetVal()).append("\");\n");
            }
        }
        builder.append("\n            default:\n");
        builder.append("                return super.callObjectMethodV(vm, dvmObject, signature, vaList);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printNewObjectV(List<JniTraceDTO> traces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<String, String>() {{
            put("java/io/File-><init>(Ljava/lang/String;)V", "dvmClass.newObject(vaList.getObjectArg(0).getValue().toString())");
            put("java/lang/Integer-><init>(I)V", "DvmInteger.valueOf(vm, vaList.getIntArg(0))");
        }};

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {\n");
        builder.append("        printArgs(vm,\"newObjectV\", signature, vaList);\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jmethodID");
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method)
                    .append(",参数:").append(trace.getArgs().get("va_list"))
                    .append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime())
                .append(",参数:").append(trace.getArgs().get("va_list"))
                .append(",样例:").append(trace.getJniRetVal()).append("\n");
            if (answer.containsKey(method)) {
                builder.append("                return ").append(answer.get(method)).append(";\n");
            } else {
                builder.append("                return super.newObjectV(vm, dvmClass, signature, vaList);\n");
            }
        }
        builder.append("\n            default:\n");
        builder.append("                return super.newObjectV(vm, dvmClass, signature, vaList);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printCallBooleanMethodV(List<JniTraceDTO> traces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<>();

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public boolean callBooleanMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {\n");
        builder.append("        printArgs(vm,\"callBooleanMethodV\", signature, vaList);\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jobject") + "->" + trace.getArgs().get("jmethodID");
            if (method.startsWith("0x")) {
                continue;
            }
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method)
                    .append(",参数:").append(trace.getArgs().get("va_list"))
                    .append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime())
                .append(",参数:").append(trace.getArgs().get("va_list"))
                .append(",样例:").append(trace.getJniRetVal()).append("\n");
            builder.append("                return ").append(answer.getOrDefault(method, trace.getJniRetVal())).append(";\n");
        }
        builder.append("\n            default:\n");
        builder.append("                return super.callBooleanMethodV(vm, dvmObject, signature, vaList);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printCallStaticLongMethodV(List<JniTraceDTO> traces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<String, String>() {{
            put("java/lang/System->currentTimeMillis()J", "System.currentTimeMillis()");
        }};

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public long callStaticLongMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {\n");
        builder.append("        printArgs(vm,\"callStaticLongMethodV\", signature, vaList);\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jmethodID");
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method)
                    .append(",参数:").append(trace.getArgs().get("va_list"))
                    .append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime())
                .append(",参数:").append(trace.getArgs().get("va_list"))
                .append(",样例:").append(trace.getJniRetVal()).append("\n");
            builder.append("                return ").append(answer.getOrDefault(method, trace.getJniRetVal())).append(";\n");
        }
        builder.append("\n            default:\n");
        builder.append("                return super.callStaticLongMethodV(vm, dvmClass, signature, vaList);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printGetObjectField(List<JniTraceDTO> traces, List<JniTraceDTO> originTraces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<>();

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jobject") + "->" + trace.getArgs().get("jfieldID");
            String str = getStringByAddr(originTraces, trace, StringUtils.substringAfterLast(trace.getJniRetVal(), ":").trim());
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method).append(",样例:").append(str).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime()).append(",样例:").append(str).append("\n");
            if (answer.containsKey(method)) {
                builder.append("                return ").append(answer.get(method)).append("\");\n");
            } else {
                builder.append("                return ").append("new StringObject(vm, \"").append(str).append("\");\n");
            }
        }
        builder.append("\n            default:\n");
        builder.append("                return super.getObjectField(vm, dvmObject, signature);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printGetStaticObjectField(List<JniTraceDTO> traces, List<JniTraceDTO> originTraces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<>();
        traces = StreamEx.of(traces).sortedBy(trace -> trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jfieldID")).toList();
        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = trace.getArgs().get("jclass") + "->" + trace.getArgs().get("jfieldID");
            if (method.startsWith("0x")) {
                continue;
            }
            String str = getStringByAddr(originTraces, trace, StringUtils.substringAfterLast(trace.getJniRetVal(), ":").trim());
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method).append(",样例:").append(str).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime()).append(",样例:").append(str).append("\n");
            builder.append("                return ").append("new StringObject(vm, \"").append(answer.getOrDefault(method, str)).append("\");\n");
        }
        builder.append("\n            default:\n");
        builder.append("                return super.getStaticObjectField(vm, dvmClass, signature);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private String printGetIntField(List<JniTraceDTO> traces, List<JniTraceDTO> originTraces) {
        if (CollectionUtils.isEmpty(traces)) {
            return "";
        }
        Set<String> check = new HashSet<>();
        Map<String, String> answer = new HashMap<>();

        StringBuilder builder = new StringBuilder("\n");
        builder.append("    @Override public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {\n");
        builder.append("        switch (signature) {\n");
        for (JniTraceDTO trace : traces) {
            String method = getClassByAddr(originTraces, trace, (String) trace.getArgs().get("jobject"))
                + "->" + trace.getArgs().get("jfieldID");
            if (check.contains(method)) {
                builder.append("            //时间:").append(trace.getTime()).append(",signature:").append(method).append(",样例:").append(trace.getJniRetVal()).append("\n");
                continue;
            }
            check.add(method);
            builder.append("\n            case \"").append(method).append("\": \n");
            builder.append("                //时间:").append(trace.getTime()).append(",样例:").append(trace.getJniRetVal()).append("\n");
            builder.append("                return ").append(answer.getOrDefault(method, trace.getJniRetVal())).append(";\n");
        }
        builder.append("\n            default:\n");
        builder.append("                return super.getIntField(vm, dvmObject, signature);\n");
        builder.append("        }\n");
        builder.append("    }");
        return builder.toString();
    }

    private List<JniTraceDTO> parseLogs(File jniTraceLogFile) throws IOException, DecoderException {
        List<String> lines = FileUtils.readLines(jniTraceLogFile, "utf-8");
        List<JniTraceDTO> traces = new ArrayList<>(lines.size() / 5);
        JniTraceDTO traceDTO = null;
        String[] kv;
        String key, val, newLine;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().startsWith("/*")) {
                traceDTO = new JniTraceDTO();
                traceDTO.setThreadId(getThreadId(line));
            }
            if (null == traceDTO) {
                continue;
            }
            // jni 函数 8810 ms [+] JNIEnv->NewObjectV
            if (line.contains("[+]")) {
                traceDTO.setJniMethodName(StringUtils.substringAfterLast(line, "[+] "));
                traceDTO.setTime(StringUtils.trimToEmpty(StringUtils.substringBeforeLast(line, " [+]")));
            }  // jni 描述 8810 ms |-> 0x7fdce017: libmtguard.so!0xe017 (libmtguard.so:0x7fdc0000)
            else if (line.contains("|-> ")) {
                traceDTO.setLibDesc(StringUtils.substringAfterLast(line, "|-> "));
                traces.add(traceDTO);
                traceDTO = null;
            } // jni函数入参 8810 ms |- jclass           : 0x29    { java/lang/String }
            else if (line.contains("|- ")) {
                line = StringUtils.substringAfter(line, "|- ");
                kv = new String[2];
                kv[0] = StringUtils.substringBefore(line, ":");
                kv[1] = StringUtils.substringAfter(line, ":");
                key = kv[0].trim();
                val = kv[1].contains("{") ? StringUtils.substringBetween(kv[1], "{", "}") : kv[1];
                switch (key) {
                    case "jbyte*":
                        byte[] bytes = null;
                        for (; lines.get(i + 1).contains("|:"); i++) {
                            newLine = StringUtils.substringAfterLast(lines.get(i + 1), ": ")
                                .substring(0, 48).replace(" ", "");
                            bytes = ArrayUtils.addAll(bytes, Hex.decodeHex(newLine));
                        }
                        traceDTO.getArgs().put(key, bytes);
                        break;
                    case "va_list":
                        Map<String, String> args = new HashMap<>(10);
                        for (; lines.get(i + 1).contains("|:"); i++) {
                            newLine = StringUtils.substringAfterLast(lines.get(i + 1), "|:");
                            kv = newLine.split(":");
                            args.put(kv[0].trim(), kv[1].trim());
                        }
                        traceDTO.getArgs().put("va_list", args);
                        break;
                    case "char*":
                        key = val.trim();
                        val = StringUtils.substringAfterLast(lines.get(i + 1), "|:");
                        if (!Objects.isNull(val)) {
                            i++;
                        }
                        traceDTO.getArgs().put(key, val.trim());
                        break;
                    default:
                        traceDTO.getArgs().put(key, val.trim());
                        break;
                }
            } // jni 函数结果
            else if (line.contains("|= ")) {
                traceDTO.setJniRetVal(StringUtils.substringAfterLast(line, "|= "));
            }
        }
        String retVal;
        for (JniTraceDTO trace : traces) {
            retVal = StringUtils.defaultString(trace.getJniRetVal());

            if (retVal.contains("jboolean")) {
                trace.setJniRetVal(StringUtils.substringBetween(retVal, "{ ", " }"));
            } else if (StringUtils.containsAnyIgnoreCase(retVal, "jlong", "jint", "jfloat")) {
                trace.setJniRetVal(StringUtils.substringAfterLast(retVal, ":").trim());
            } else if (retVal.contains("java/lang/String") || (retVal.contains("{ java/lang/Object }") && trace.getJniMethodName().contains("CallStaticObjectMethod"))) {
                trace.setJniRetVal(getStringByAddr(traces, trace, StringUtils.substringBetween(retVal, ":", "{").trim()));
            }

            if (trace.getArgs().containsKey("jclass")) {
                trace.getArgs().put("jclass", getClassByAddr(traces, trace, (String) trace.getArgs().get("jclass")));
            }

            Map<String, String> vaList = (HashMap<String, String>) trace.getArgs().getOrDefault("va_list", new HashMap<>());

            if (COMMON_METHOD.contains(trace.getJniMethodName()) && vaList.containsKey("jstring")) {
                //log.info("time:{},method:{},valist:{}", trace.getTime(), trace.getJniMethodName(), vaList);
                vaList.put("jstring", getStringPreAddr(traces, trace, vaList.get("jstring")));
                trace.getArgs().put("va_list", vaList);
            }
        }
        return traces;
    }

    private String getStringByAddr(List<JniTraceDTO> traces, JniTraceDTO traceDTO, String addr) {
        JniTraceDTO nextTrace;
        String charAddr = null;
        for (int i = traces.indexOf(traceDTO); i < traces.size(); i++) {
            nextTrace = traces.get(i);
            if (!traceDTO.getThreadId().equals(nextTrace.getThreadId())) {
                continue;
            }
            if ("JNIEnv->GetStringUTFChars".equals(nextTrace.getJniMethodName())
                && addr.equals(nextTrace.getArgs().get("jstring"))) {
                charAddr = StringUtils.substringAfterLast(nextTrace.getJniRetVal(), ":").trim();
            }
            if (StringUtils.isNotBlank(charAddr)
                && "JNIEnv->ReleaseStringUTFChars".equals(nextTrace.getJniMethodName())
                && nextTrace.getArgs().containsKey(charAddr)) {
                return (String) nextTrace.getArgs().get(charAddr);
            }
        }
        return "";
    }

    private String getStringPreAddr(List<JniTraceDTO> traces, JniTraceDTO traceDTO, String addr) {
        JniTraceDTO preTrace;
        String charAddr = null, jniMethod = "", key = "", retKey = "";

        for (int i = traces.indexOf(traceDTO); i > 0; i--) {
            preTrace = traces.get(i);
            if (!traceDTO.getThreadId().equals(preTrace.getThreadId())) {
                continue;
            }
            if ("JNIEnv->NewObjectV".equals(preTrace.getJniMethodName())) {
                if (preTrace.getArgs().getOrDefault("jclass", "").toString().contains("java/lang/String")) {
                    Map<String, String> valist = (Map<String, String>) preTrace.getArgs().get("va_list");
                    if (!CollectionUtils.isEmpty(valist) && addr.equals(valist.getOrDefault("jstring", ""))) {
                        charAddr = valist.get("jbyteArray");
                        jniMethod = "JNIEnv->SetByteArrayRegion";
                        key = "jbyteArray";
                        retKey = "jbyte*";
                    }
                }

            }
            if (StringUtils.isNotBlank(charAddr)
                && jniMethod.equals(preTrace.getJniMethodName())
                && preTrace.getArgs().get(key).equals(charAddr)) {
                if ("jbyte*".equals(retKey)) {
                    byte[] ret = (byte[]) preTrace.getArgs().get(retKey);
                    return "hex:" + Hex.encodeHexString(ret) + ", string:" + new String(ret);
                }
                return (String) preTrace.getArgs().get(retKey);
            }
        }

        for (int i = traces.indexOf(traceDTO); i > 0; i--) {
            preTrace = traces.get(i);
            if (!traceDTO.getThreadId().equals(preTrace.getThreadId())) {
                continue;
            }
            if ("JNIEnv->NewObjectV".equals(preTrace.getJniMethodName())
                || StringUtils.defaultString(preTrace.getJniRetVal()).contains(addr)) {
                Map<String, String> valist = (Map<String, String>) preTrace.getArgs().getOrDefault("va_list", new HashMap<>());
                return valist.getOrDefault("jstring", "");
            }
        }
        return "";
    }

    private String getClassByAddr(List<JniTraceDTO> traces, JniTraceDTO traceDTO, String addr) {
        if (!addr.startsWith("0x")) {
            return addr;
        }
        // JNIEnv->FindClass
        JniTraceDTO nextTrace;
        for (int i = traces.indexOf(traceDTO); i < traces.size(); i++) {
            nextTrace = traces.get(i);
            if (!traceDTO.getThreadId().equals(nextTrace.getThreadId())) {
                continue;
            }
            if ("JNIEnv->FindClass".equals(nextTrace.getJniMethodName())
                && nextTrace.getJniRetVal().contains(addr)) {
                return StringUtils.substringBetween(nextTrace.getJniRetVal(), "{ ", " }");
            }
        }
        return addr;
    }

    private String getThreadId(String str) {
        return str.trim().split(" ")[2];
    }

}
