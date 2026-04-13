package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class SequenceIdGeneratorTest {
    @Test
    void getNextIdShouldReturnIncrementingValuesStartingFromOne() {
        SequenceIdGenerator generator = new SequenceIdGenerator();

        assertThat(generator.getNextId()).isEqualTo(1L);
        assertThat(generator.getNextId()).isEqualTo(2L);
        assertThat(generator.getNextId()).isEqualTo(3L);
    }

    @Test
    void getNextIdShouldBeThreadSafe() throws Exception {
        SequenceIdGenerator generator = new SequenceIdGenerator();
        int threadCount = 10;
        int requestsPerThread = 100;
        int totalRequests = threadCount * requestsPerThread;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        Set<Long> generatedIds = ConcurrentHashMap.newKeySet();

        try {
            Future<?>[] futures = new Future<?>[threadCount];
            for (int i = 0; i < threadCount; i++) {
                futures[i] = executorService.submit(() -> {
                    startLatch.await();
                    for (int j = 0; j < requestsPerThread; j++) {
                        generatedIds.add(generator.getNextId());
                    }
                    return null;
                });
            }
            startLatch.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } finally {
            executorService.shutdownNow();
        }

        assertThat(generatedIds).hasSize(totalRequests);
        assertThat(generatedIds).contains(1L, (long) totalRequests);
    }
}
