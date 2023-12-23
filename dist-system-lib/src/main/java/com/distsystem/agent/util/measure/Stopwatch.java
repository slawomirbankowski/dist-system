package com.distsystem.agent.util.measure;

public class Stopwatch {
  private final long t0;

  public static Stopwatch start() {
    return new Stopwatch();
  }

  private Stopwatch() {
    this.t0 = System.nanoTime();
  }

  public long tick() {
    return System.nanoTime() - t0;
  }

  public String tickPretty() {
    return TimedResult.prettyNs(tick());
  }
}
