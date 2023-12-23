package com.distsystem.agent.util;

import java.util.function.Function;

@FunctionalInterface
public interface EFunction<T, R> {
  R apply(T t) throws Exception;

  default Function<T, R> toFunction() {
    return (t) -> {
      try {
        return apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }
}
