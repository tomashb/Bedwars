# Bedwars

Squelette minimal d'un plugin BedWars pour Spigot/Paper 1.21.

## Compilation

```bash
mvn -DskipTests package
```

Le JAR généré se trouve dans `target/bedwars-0.0.1.jar` et doit être
placé dans le dossier `plugins/` d'un serveur Paper/Spigot 1.21.

## Éditeur d'arène (Étape 4)

Un assistant permet de créer une arène via le menu admin (laine verte).
Après saisie de l'identifiant, l'éditeur s'ouvre avec les actions
suivantes (slots principaux) :

| Slot | Action |
|-----:|--------|
| 10 | Définir le lobby |
| 12 | Ouvrir la gestion des équipes |
| 14 | Ajouter des PNJ |
| 16 | Ajouter des générateurs |
| 28 | Sauvegarder |
| 30 | Recharger |
| 32 | Supprimer |
| 49 | Retour |

Les sous-menus permettent d'activer une équipe, de positionner ses spawns
et lits, de poser les PNJ boutique/upgrade et d'ajouter les générateurs
marqués.
