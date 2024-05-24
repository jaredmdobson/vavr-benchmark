package io.vavr.collection;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class CollectionBenchmarkBase {
  @Param({"1000"})
  public int CONTAINER_SIZE;
}