package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.utils.Inspector;
import com.github.unidbg.worker.Worker;
import com.github.unidbg.worker.WorkerLoan;
import com.github.unidbg.worker.WorkerPool;
import com.github.unidbg.worker.WorkerPoolFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("ttEncryptWorker")
public class TTEncryptServiceWorker implements Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private TTEncryptService ttEncryptService;
    @Value("${spring.task.execution.pool.core-size:4}")
    int poolSize;

    @Autowired
    public void init(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
    }

    public TTEncryptServiceWorker() {
        pool = WorkerPoolFactory.create(TTEncryptServiceWorker::new, poolSize);
    }

    public TTEncryptServiceWorker(WorkerPool pool) {
        this.pool = pool;
    }

    @Autowired
    public TTEncryptServiceWorker(UnidbgProperties unidbgProperties,
        @Value("${spring.task.execution.pool.core-size:4}") int configuredPoolSize) {
        this.unidbgProperties = unidbgProperties;
        if (this.unidbgProperties.isAsync()) {
            int effectivePoolSize = workerPoolSize(configuredPoolSize);
            pool = WorkerPoolFactory.create(() -> new TTEncryptServiceWorker(unidbgProperties.isDynarmic(),
                unidbgProperties.isVerbose(), pool), effectivePoolSize);
            log.info("线程池为:{}", effectivePoolSize);
        } else {
            this.ttEncryptService = new TTEncryptService(unidbgProperties);
        }
    }

    public TTEncryptServiceWorker(boolean dynarmic, boolean verbose, WorkerPool pool) {
        this.pool = pool;
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", dynarmic, verbose);
        this.ttEncryptService = new TTEncryptService(unidbgProperties);
    }

    @Async
    @SneakyThrows
    public CompletableFuture<byte[]> ttEncrypt(String key1, String body) {

        byte[] data;
        TTEncryptServiceWorker worker;
        long start = System.currentTimeMillis();
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                try (WorkerLoan<TTEncryptServiceWorker> loan = pool.borrow(2, TimeUnit.SECONDS)) {
                    if (loan == null) {
                        continue;
                    }
                    worker = loan.get();
                    data = worker.doWork(key1, body);
                    break;
                }
            }
        } else {
            synchronized (this) {
                data = this.doWork(key1, body);
            }
        }

        long currentTimeMillis = System.currentTimeMillis();
        Inspector.inspect(data, Thread.currentThread().getName() + ": " + (System.currentTimeMillis() - start) + "ms" + ", " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
        return CompletableFuture.completedFuture(data);
    }

    static int workerPoolSize(int configuredPoolSize) {
        return Math.max(configuredPoolSize, 4);
    }

    private byte[] doWork(String key1, String body) {
        return ttEncryptService.ttEncrypt(body);
    }

    @SneakyThrows
    @Override
    public void destroy() {
        ttEncryptService.destroy();
    }
}
