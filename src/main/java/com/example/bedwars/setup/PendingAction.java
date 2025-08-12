package com.example.bedwars.setup;

public record PendingAction(EditorActions action, Object payload, long expiresAt) {}
