package com.example.bedwars.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import com.example.bedwars.gui.editor.EditorView;

public final class BWMenuHolder implements InventoryHolder {
  public final AdminView view;
  public final EditorView editorView; // non nul pour sous-vues éditeur
  public final String arenaId; // null sur ROOT ou vues globales

  public BWMenuHolder(AdminView view, String arenaId) {
    this.view = view;
    this.editorView = null;
    this.arenaId = arenaId;
  }

  public BWMenuHolder(EditorView editor, String arenaId) {
    this.view = AdminView.ARENA_EDITOR;
    this.editorView = editor;
    this.arenaId = arenaId;
  }

  @Override
  public Inventory getInventory() {
    return null; // non utilisé
  }
}
