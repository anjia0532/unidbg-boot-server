package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("ttEncryptWorker")
public class TTEncryptWorker implements Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private TTEncrypt ttEncrypt;

    public TTEncryptWorker() {

    }

    @Autowired
    public TTEncryptWorker(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
        this.ttEncrypt = new TTEncrypt(unidbgProperties);
        pool = WorkerPoolFactory.create(() ->
                        new TTEncryptWorker(unidbgProperties.isDynarmic(), unidbgProperties.isVerbose()),
                Runtime.getRuntime().availableProcessors());
    }

    public TTEncryptWorker(boolean dynarmic, boolean verbose) {
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        this.ttEncrypt = new TTEncrypt(unidbgProperties);
    }

    @Async
    public CompletableFuture<byte[]> ttEncrypt() {

        TTEncryptWorker worker = pool.borrow(2, TimeUnit.SECONDS);
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
        ttEncrypt.destroy();
        System.err.println("Destroy: " + ttEncrypt);
    }

    private byte[] doWork() {
        return ttEncrypt.ttEncrypt();
    }

}
