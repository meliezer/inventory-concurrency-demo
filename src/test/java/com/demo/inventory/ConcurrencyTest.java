package com.demo.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyTest {

  @Autowired
  private OrderService service;

  @Autowired
  private InventoryRepository repo;

  @BeforeEach
  void setup() {
    repo.deleteAll();
    repo.saveAndFlush(new Inventory("SKU-1", 10));
  }

  @Test
  void shouldProduceInconsistentResultWithoutLocking() throws Exception {
    int threads = 20;

    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      CountDownLatch ready = new CountDownLatch(threads);
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(threads);

      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          ready.countDown();
          try {
            start.await();
            service.order("SKU-1", 1);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          } catch (OutOfStockException ignored) {
            // expected once stock is exhausted
          } finally {
            done.countDown();
          }
        });
      }

      ready.await();
      start.countDown();
      done.await();
    }

    Inventory inv = repo.findById("SKU-1").orElseThrow();

    // Demonstrates lost updates without explicit concurrency control.
    assertNotEquals(0, inv.getQuantity());
  }

  @Test
  void shouldNotOversellWithPessimisticLocking() throws Exception {
    int threads = 20;

    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      CountDownLatch ready = new CountDownLatch(threads);
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(threads);

      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          ready.countDown();
          try {
            start.await();
            service.orderWithLock("SKU-1", 1);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          } catch (OutOfStockException ignored) {
            // expected once stock is exhausted
          } finally {
            done.countDown();
          }
        });
      }

      ready.await();
      start.countDown();
      done.await();
    }

    Inventory inv = repo.findById("SKU-1").orElseThrow();

    // Ensures correctness via pessimistic locking (blocking).
    assertEquals(0, inv.getQuantity());
  }

  @Test
  void shouldNotOversellWithOptimisticLockingRetry() throws Exception {
    int threads = 20;

    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      CountDownLatch ready = new CountDownLatch(threads);
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(threads);

      AtomicInteger successCount = new AtomicInteger();
      AtomicInteger failureCount = new AtomicInteger();

      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          ready.countDown();
          try {
            start.await();
            service.orderWithOptimisticRetry("SKU-1", 1);
            successCount.incrementAndGet();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          } catch (OutOfStockException | OrderFailedException e) {
            failureCount.incrementAndGet();
          } finally {
            done.countDown();
          }
        });
      }

      ready.await();
      start.countDown();
      done.await();
      assertEquals(20, successCount.get() + failureCount.get());
    }

    Inventory inv = repo.findById("SKU-1").orElseThrow();

    // Ensures correctness via optimistic locking with retry.
    assertEquals(0, inv.getQuantity());
  }
}
