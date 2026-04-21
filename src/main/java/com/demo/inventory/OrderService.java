package com.demo.inventory;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final InventoryCommandService commandService;

  public OrderService(InventoryCommandService commandService) {
    this.commandService = commandService;
  }

  public void order(String sku, int amount) {
    commandService.orderOnce(sku, amount);
  }

  public void orderWithLock(String sku, int amount) {
    commandService.orderOnceWithPessimisticLock(sku, amount);
  }

  public void orderWithOptimisticRetry(String sku, int amount) {
    int maxAttempts = 5;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        commandService.orderOnce(sku, amount);
        return;
      } catch (ObjectOptimisticLockingFailureException e) {
        if (attempt == maxAttempts) {
          throw new OrderFailedException(
              "Failed to complete order after " + maxAttempts + " attempts",
              e
          );
        }

        try {
          Thread.sleep(10L);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new OrderFailedException("Retry interrupted", ie);
        }
      }
    }
  }
}
