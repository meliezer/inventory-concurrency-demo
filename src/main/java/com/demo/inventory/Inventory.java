package com.demo.inventory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Inventory {

  @Id
  private String sku;

  private int quantity;

  @Version
  private int version;

  protected Inventory() {
    // for JPA
  }

  public Inventory(String sku, int quantity) {
    this.sku = sku;
    this.quantity = quantity;
  }

  public String getSku() {
    return sku;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getVersion() {
    return version;
  }

  public void decreaseQuantity(int amount) {
    this.quantity -= amount;
  }
}
