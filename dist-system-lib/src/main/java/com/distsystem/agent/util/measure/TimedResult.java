package com.distsystem.agent.util.measure;

import java.util.function.Supplier;

public class TimedResult<T> {
  public static <T> TimedResult<T> timed(Supplier<T> op) {
    var t0 = System.nanoTime();
    var res = op.get();
    var diff = System.nanoTime() - t0;
    return new TimedResult<>(res, diff);
  }

  private final T result;
  private final long elapsedNs;

  public TimedResult(T result, long elapsedNs) {
    this.result = result;
    this.elapsedNs = elapsedNs;
  }

  public T getResult() {
    return result;
  }

  public long getElapsedNs() {
    return elapsedNs;
  }

  public double getElapsedMs() {
    return elapsedNs / 1_000_000.0;
  }

  public static String prettyNs(long elapsedNs) {
    if (elapsedNs < 1000) return String.format("%d ns", elapsedNs);
    else if (elapsedNs < 1_000_000) return String.format("%.2f \u00B5s", elapsedNs / 1_000.0);
    else if (elapsedNs < 1_000_000_000) return String.format("%.2f ms", elapsedNs / 1_000_000.0);
    else return String.format("%.2f s", elapsedNs / 1_000_000_000.0);
  }

  @Override
  public String toString() {
    return "TimedResult{" +
        "result=" + result +
        ", elapsed=" + prettyNs(elapsedNs) +
        '}';
  }
}
