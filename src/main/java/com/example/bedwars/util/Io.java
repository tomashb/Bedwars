package com.example.bedwars.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class Io {
  private static final ExecutorService POOL = Executors.newFixedThreadPool(
      Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

  private Io() {}

  public static <T> CompletableFuture<T> async(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, POOL);
  }
}
