package com.distsystem.agent.util;

import java.util.function.Supplier;

public interface ESupplier<T> {
  T get() throws Exception;

  default Supplier<T> toSupplier() {
    return () -> {
      try {
        return get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
