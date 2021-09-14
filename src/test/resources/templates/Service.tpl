package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.EmulatorBuilder;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.ApplicationInfo;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.linux.android.dvm.wrapper.DvmLong;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import static com.anjia.unidbgserver.utils.PrintUtils.*;
import com.github.unidbg.memory.Memory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;

@Slf4j
public class [(${ServiceName})]Service extends AbstractJni implements IOResolver {

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    private final static String BASE_[(${#strings.toUpperCase(ServiceName)})]_PATH = "data/apks/[(${#strings.toLowerCase(ServiceName)})]";
    private final static String [(${#strings.toUpperCase(ServiceName)})]_APK_PATH = BASE_[(${#strings.toUpperCase(ServiceName)})]_PATH + "/[(${#strings.toLowerCase(ServiceName)})].apk";

    private final UnidbgProperties unidbgProperties;

    // private final static String LIBTT_ENCRYPT_LIB_PATH = "data/apks/so/libttEncrypt.so";

    @SneakyThrows [(${ServiceName})]Service(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
        EmulatorBuilder<AndroidEmulator> builder = AndroidEmulatorBuilder.for32Bit().setProcessName("com.xxxxx");
        // 动态引擎
        if (unidbgProperties.isDynarmic()) {
            builder.addBackendFactory(new DynarmicFactory(true));
        }
        emulator = builder.build();
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));

        // 创建Android虚拟机
        // vm = emulator.createDalvikVM(); // 只创建vm，用来读so,不加载apk
        vm = emulator.createDalvikVM(TempFileUtils.getTempFile([(${#strings.toUpperCase(ServiceName)})]_APK_PATH));
        // 设置是否打印Jni调用细节
        vm.setVerbose(unidbgProperties.isVerbose());
        vm.setJni(this);
        emulator.getSyscallHandler().addIOResolver(this);
        // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数，这是直接读so文件
        // DalvikModule dm = vm.loadLibrary(TempFileUtils.getTempFile(LIBTT_ENCRYPT_LIB_PATH), false);
        // 这是搜索加载apk里的模块名，比如 libguard.so 那么模块名一般是guard
        DalvikModule dm = vm.loadLibrary("模块名", false);
        // 手动执行JNI_OnLoad函数
        dm.callJNI_OnLoad(emulator);
        // 加载好的libttEncrypt.so对应为一个模块
        module = dm.getModule();

        dm.callJNI_OnLoad(emulator);

        // TTEncryptUtils = vm.resolveClass("com/bytedance/frameworks/core/encrypt/TTEncryptUtils");
    }

    /**
     * unidbg 模拟调用
     *
     * @param param 入参
     * @return 结果
     */
    public Object doWork(Object param) {
        return null;
    }

[(${content})]

    @SneakyThrows @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        switch (pathname) {
            default:
                printFileResolve(pathname);
                return null;
        }
    }


    public void destroy() throws IOException {
        emulator.close();
    }
}
