# Bedwars

Plugin BedWars pour Spigot/Paper 1.21.

## Mise en route rapide

1. Compilez le plugin : `mvn -DskipTests package`.
2. Copiez `target/bedwars-0.2.0.jar` dans le dossier `plugins/` de votre serveur 1.21.
3. Démarrez le serveur puis utilisez `/bw` pour créer une arène :
   - `/bw create <nom>` crée l'arène et en fait votre arène de configuration.
   - Placez le lobby, les lits, spawns, générateurs et PNJ via les sous-commandes du menu.
   - `/bw save` enregistre l'arène dans `plugins/Bedwars/arenas/`.
4. Rejoignez l'arène avec `/bw join <nom>` et utilisez `/bw start` pour lancer la partie.
5. À la fin, l'arène est automatiquement réinitialisée.

## Notes de test

1. Créer/éditer une arène avec `/bw` puis poser les générateurs et PNJ.
2. Démarrer l'arène : les marqueurs « GEN: ... » de setup disparaissent.
3. Clic droit sur le PNJ « Améliorations » ouvre le menu dédié aux upgrades d'équipe.
4. Acheter de la laine dans la boutique d'objets donne de la laine à la couleur de votre équipe.
5. Le scoreboard ne liste que les équipes activées pour l'arène en cours.

Pour plus de détails sur l'architecture et la machine à états, voir [ARCHITECTURE.md](ARCHITECTURE.md).
