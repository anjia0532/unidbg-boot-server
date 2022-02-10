package com.anjia.unidbgserver;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.memory.Memory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * so 函数打印
 *
 * @author AnJia
 * @since 2021-09-14 15:00
 */
@Slf4j
@SpringBootTest
public class RegisterNativeTest  extends AbstractJni {

    private AndroidEmulator emulator;
    private VM vm;

    @Test
    public void generatorTest() throws IOException {

        // 写你代码,不会写的，参考下面的test()
    }
    //@Test
    public void test() throws IOException {
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.anjia.test").build();
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));

        // 创建Android虚拟机
        vm = emulator.createDalvikVM(new File("com.sankuai.meituan_11.13.207_1100130207.apk"));
        // 设置是否打印Jni调用细节
        vm.setVerbose(true);
        vm.setJni(this);


        List<String> denyList = Arrays.asList("libnodelibnode.so", "libv8.so", "libmtmap.so","libttEncrypt.so");

        for (File file : Objects.requireNonNull(new File("src/main/resources/data/apks/so/").listFiles())) {
            if (denyList.contains(file.getName())) {
                continue;
            }
            try {
                DalvikModule dm = vm.loadLibrary(file.getName().replaceAll("lib|\\.so", ""), true);
                dm.callJNI_OnLoad(emulator);
            } catch (Exception ignored) {
                log.info("error：{}", file.getName());
            }
        }
    }
}
