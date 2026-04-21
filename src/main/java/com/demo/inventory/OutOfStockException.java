package com.demo.inventory;

public class OutOfStockException extends RuntimeException {

  public OutOfStockException(String sku) {
    super("Out of stock for sku: " + sku);
  }
}
