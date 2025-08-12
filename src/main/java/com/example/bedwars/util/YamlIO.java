package com.example.bedwars.util;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Small helpers for reading and writing YAML files.
 */
public final class YamlIO {
  private YamlIO() {
  }

  public static YamlConfiguration load(File file) {
    return YamlConfiguration.loadConfiguration(file);
  }

  public static void save(File file, YamlConfiguration cfg) throws IOException {
    cfg.save(file);
  }
}
