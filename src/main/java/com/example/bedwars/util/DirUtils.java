package com.example.bedwars.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

/** Utility methods for directory operations. */
public final class DirUtils {
  private DirUtils() {}

  public static void deleteDir(Path path) throws IOException {
    if (!Files.exists(path)) return;
    try (Stream<Path> s = Files.walk(path)) {
      s.sorted(Comparator.reverseOrder()).forEach(p -> {
        try { Files.delete(p); } catch (IOException ignored) {}
      });
    }
  }

  public static void copyDir(Path src, Path dest) throws IOException {
    try (Stream<Path> s = Files.walk(src)) {
      for (Path p : (Iterable<Path>) s::iterator) {
        Path target = dest.resolve(src.relativize(p));
        if (Files.isDirectory(p)) {
          Files.createDirectories(target);
        } else {
          Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
      }
    }
  }
}
