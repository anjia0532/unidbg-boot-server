package com.anjia.unidbgserver.utils;

import com.github.unidbg.linux.android.dvm.DvmObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.VaList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 打印工具类
 *
 * @author AnJia
 * @since 2021-09-12 15:35
 */
@Slf4j
public class PrintUtils {

    /**
     * 打印读取本地文件，生成伪代码
     *
     * @param pathname 文件名
     */
    public static void printFileResolve(String pathname) {
        printFileResolve(pathname, null);
    }


    /**
     * 打印读取本地文件，生成伪代码
     *
     * @param pathname      文件名
     * @param localPathName 本地文件名，可以为null
     */
    public static void printFileResolve(String pathname, String localPathName) {
        String builder = "\n" + "            case \"" + pathname + "\": {\n" +
                "                return FileResult.success(new SimpleFileIO(oflags, TempFileUtils.getTempFile(\""
                + StringUtils.defaultString(localPathName) + "\"), pathname));\n" +
                "            }";
        log.debug(builder);
    }

    /**
     * 打印各种MethodV的签名，入参
     *
     * @param vm        vm
     * @param signature 方法签名
     * @param vaList    入参
     */
    public static void printArgs(VM vm, String signature, VaList vaList) {
        printArgs(vm, null, signature, vaList);
    }

    /**
     * 打印各种MethodV的签名，入参
     *
     * @param vm         vm
     * @param methodName 各种MethodV的方法名
     * @param signature  方法签名
     * @param vaList     入参
     */
    public static void printArgs(VM vm, String methodName, String signature, VaList vaList) {
        StringBuilder argsBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(methodName)) {
            builder.append("调用方法名:").append(methodName);
        }
        builder.append(",方法签名:").append(signature);
        DvmObject<?> obj;
        int hash, i = 0;
        if (Objects.isNull(vaList)) {
            return;
        }
        try {
            for (; ; i++) {

                hash = vaList.getIntArg(i);
                if (!Objects.isNull(vm)) {
                    obj = vm.getObject(hash);
                } else {
                    obj = null;
                }
                argsBuilder.append(",参数:").append(i);
                if (Objects.isNull(obj)) {
                    argsBuilder.append(",值:").append(hash);
                } else {
                    if (!Objects.isNull(obj.getObjectType())) {
                        argsBuilder.append(",类型:").append(obj.getObjectType().getClassName());
                    }
                    argsBuilder.append(",值:").append(obj.getValue());
                }
            }
        } catch (Exception ex) {
            if (i > 0) {
                builder.append(argsBuilder);
            } else {
                builder.append(",无参数");
            }
        }
        log.debug(builder.toString());
    }
}
