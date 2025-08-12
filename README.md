# Bedwars (Skeleton)

Ce dépôt contient une implémentation minimale d'un plugin BedWars pour Spigot/Paper 1.21.
Il s'agit d'un point de départ expérimental : seules quelques fonctionnalités
essentielles sont fournies (multi-arène simplifié, commande `/bw` de base,
compte à rebours). L'objectif est de montrer comment structurer le code pour
une réécriture complète.

## Mise en route rapide

1. Compiler le plugin : `mvn -DskipTests package`.
2. Copier `target/bedwars-0.2.2.jar` dans le dossier `plugins/` du serveur.
3. Démarrer le serveur et utiliser `/bw list` pour voir les arènes disponibles.
4. Rejoindre une arène avec `/bw join <nom>` puis quitter avec `/bw leave`.

## Limites

Ce plugin n'implémente pas encore l'ensemble des mécaniques BedWars
(générateurs, boutiques, upgrades, scoreboard, etc.).
Il constitue simplement une base sur laquelle bâtir.

## Administration

Un menu de gestion basique est disponible pour les joueurs possédant la
permission `bedwars.admin`.

* `/bw menu` (alias `/bw admin`, `/bw setup`) : ouvre la GUI de gestion.
* `/bwadmin arena list` : liste les arènes chargées.
* `/bwadmin arena create <id> <monde>` : crée une arène minimale.
* `/bwadmin arena delete <id>` : supprime une arène et son fichier.
* `/bwadmin game events <arène> <enable|disable>` : active ou désactive les événements de partie.

Ces commandes ne couvrent qu'une petite partie du framework souhaité mais
servent de point de départ pour l'étendre.

Pour quelques notes d'architecture, voir [ARCHITECTURE.md](ARCHITECTURE.md).
