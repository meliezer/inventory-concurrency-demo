package com.demo.inventory;

public class OrderFailedException extends RuntimeException {

  public OrderFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
