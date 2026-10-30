package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TTEncryptServiceWorkerConfigTest {

    @Test
    void configuredCoreSizeIsConstructorInjectedBeforePoolCreation() throws NoSuchMethodException {
        Constructor<TTEncryptServiceWorker> constructor = TTEncryptServiceWorker.class
            .getConstructor(UnidbgProperties.class, int.class);

        assertNotNull(constructor.getAnnotation(Autowired.class));
        Value configuredSize = constructor.getParameters()[1].getAnnotation(Value.class);
        assertNotNull(configuredSize);
        assertEquals("${spring.task.execution.pool.core-size:4}", configuredSize.value());
    }

    @Test
    void workerPoolKeepsConfiguredSizeButNeverFallsBelowFour() {
        assertEquals(8, TTEncryptServiceWorker.workerPoolSize(8));
        assertEquals(4, TTEncryptServiceWorker.workerPoolSize(2));
    }
}
