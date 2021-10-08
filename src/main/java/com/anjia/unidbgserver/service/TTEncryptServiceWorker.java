package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("ttEncryptWorker")
public class TTEncryptServiceWorker implements Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private TTEncryptService ttEncryptService;


    public TTEncryptServiceWorker() {

    }

    @Autowired
    public TTEncryptServiceWorker(UnidbgProperties unidbgProperties,
                                  @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        this.unidbgProperties = unidbgProperties;
        pool = WorkerPoolFactory.create(() ->
                        new TTEncryptServiceWorker(unidbgProperties.isDynarmic(), unidbgProperties.isVerbose()),
                Math.max(poolSize, 4));
        log.info("线程池为:{}", Math.max(poolSize, 4));
    }

    public TTEncryptServiceWorker(boolean dynarmic, boolean verbose) {
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", dynarmic, verbose);
        this.ttEncryptService = new TTEncryptService(unidbgProperties);
    }

    @Async
    public CompletableFuture<byte[]> ttEncrypt() {

        TTEncryptServiceWorker worker = pool.borrow(2, TimeUnit.SECONDS);
        assert worker != null;
        byte[] data = null;
        try {
            data = worker.doWork();
        } catch (Exception ex) {
            log.error("TTEncrypt失败", ex);
        } finally {
            pool.release(worker);
        }
        return CompletableFuture.completedFuture(data);
    }

    @Override
    public void close() throws IOException {
        ttEncryptService.destroy();
        log.info("Destroy: {}", ttEncryptService);
    }

    private byte[] doWork() {
        return ttEncryptService.ttEncrypt();
    }

}
