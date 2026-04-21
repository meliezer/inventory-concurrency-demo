package com.demo.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryCommandService {

  private final InventoryRepository repo;

  public InventoryCommandService(InventoryRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public void orderOnce(String sku, int amount) {
    Inventory inv = repo.findById(sku).orElseThrow();

    if (inv.getQuantity() < amount) {
      throw new OutOfStockException(sku);
    }

    inv.decreaseQuantity(amount);
    repo.saveAndFlush(inv);
  }

  @Transactional
  public void orderOnceWithPessimisticLock(String sku, int amount) {
    Inventory inv = repo.findBySkuForUpdate(sku);

    if (inv.getQuantity() < amount) {
      throw new OutOfStockException(sku);
    }

    inv.decreaseQuantity(amount);
  }
}
