# Guide Admin BedWars

Plugin BedWars pour Spigot/Paper 1.21.

## Installation
1. Compiler le plugin : `mvn package`.
2. Déposer `target/bedwars-0.0.1.jar` dans `plugins/`.
3. Démarrer le serveur pour générer la structure :
   - `plugins/Bedwars/arenas/`
   - `plugins/Bedwars/templates/`
   - fichiers `config.yml`, `messages.yml`, `shop.yml`, `upgrades.yml`, `rotation.yml`.

## Setup rapide
1. `/bw menu` → menu admin.
2. Créer une arène → définir Lobby, Spawns/Lits pour chaque équipe.
3. Ajouter PNJ boutique & upgrades, générateurs.
4. `Save` puis `/bwadmin game start <id>` pour tester.

## Commandes & permissions
| Commande | Permission |
|---------|------------|
| `/bw help` | *(aucune)* |
| `/bw join <arène>` | *(aucune)* |
| `/bw menu` | `bedwars.admin.arena` |
| `/bwadmin arena <...>` | `bedwars.admin.arena` |
| `/bwadmin game <...>` | `bedwars.admin.game` |
| `/bwadmin debug status <arène>` | `bedwars.admin.debug` |
| `/bwadmin maintenance cleanup <arène>` | `bedwars.admin.maintenance` |

Permissions globales : `bedwars.admin.*` (ops par défaut) et gameplay :
`bedwars.menu.rules`, `bedwars.build.place`.

## Captures
Captures d'écran à insérer ici.

## FAQ
- **Je ne peux pas placer de blocs en arène** : vérifier `bedwars.build.place`.
- **Le TNT ne s'allume pas** : assurez-vous que l'arène n'est pas en mode `WAITING`.
- **Le scoreboard n'apparaît pas** : vérifier `scoreboard.enabled` dans `config.yml`.
- **Permissions** : utiliser `bedwars.admin.*` ou les sous-permissions détaillées ci-dessus.

