package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.github.unidbg.worker.Worker;
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
@Service("[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]ServiceWorker")
public class [[${ServiceName}]]ServiceWorker extends Worker {

    private UnidbgProperties unidbgProperties;
    private WorkerPool pool;
    private [[${ServiceName}]]Service [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service;

    public [[${ServiceName}]]ServiceWorker() {
        super(WorkerPoolFactory.create([[${ServiceName}]]ServiceWorker::new, Runtime.getRuntime().availableProcessors()));
    }

    public [[${ServiceName}]]ServiceWorker(WorkerPool pool) {
        super(pool);
    }

    @Autowired
    public [[${ServiceName}]]ServiceWorker(UnidbgProperties unidbgProperties,
                           @Value("${spring.task.execution.pool.core-size:4}") int poolSize) {
        super(WorkerPoolFactory.create([[${ServiceName}]]ServiceWorker::new, Runtime.getRuntime().availableProcessors()));
        this.unidbgProperties = unidbgProperties;
        this.[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service = new [[${ServiceName}]]Service(unidbgProperties);
        pool = WorkerPoolFactory.create((pool) ->
                        new [[${ServiceName}]]ServiceWorker(unidbgProperties.isDynarmic(), unidbgProperties.isVerbose(),pool),
                Math.max(poolSize, 4));
        log.info("线程池为:{}", Math.max(poolSize, 4));
    }

    public [[${ServiceName}]]ServiceWorker(boolean dynarmic, boolean verbose, WorkerPool pool) {
        super(pool);
        this.unidbgProperties = new UnidbgProperties();
        unidbgProperties.setDynarmic(dynarmic);
        unidbgProperties.setVerbose(verbose);
        log.info("是否启用动态引擎:{},是否打印详细信息:{}", dynarmic, verbose);
        this.[(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service = new [[${ServiceName}]]Service(unidbgProperties);
    }

    @Autowired
    public void init(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
    }

    @Async
    public CompletableFuture<Object> doWork(Object param) {
        [[${ServiceName}]]ServiceWorker worker;
        Object data;
        if (this.unidbgProperties.isAsync()) {
            while (true) {
                if ((worker = pool.borrow(2, TimeUnit.SECONDS)) == null) {
                    continue;
                }
                data = worker.exec(param);
                pool.release(worker);
                break;
            }
        } else {
            synchronized (this) {
                data = this.exec(param);
            }
        }
        return CompletableFuture.completedFuture(data);
    }

    @SneakyThrows
    @Override public void destroy() {
        [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service.destroy();
    }

    private Object exec(Object param) {
        return [(${#strings.toLowerCase(#strings.substring(ServiceName,0,1))})][(${#strings.substring(ServiceName,1)})]Service.doWork(param);
    }

}
