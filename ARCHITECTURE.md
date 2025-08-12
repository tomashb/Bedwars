# Architecture BedWars

Cette implémentation suit un modèle multi-arène et repose sur une machine à états simple.

## Machine à états

Chaque arène possède un `GameState` parmi `WAITING`, `STARTING`, `RUNNING`, `ENDING` et `RESTARTING`. Les transitions sont gérées par un `GameTicker` programmé chaque seconde. Il s'occupe des décomptes, de l'activation des générateurs et de la vérification des conditions de victoire.

## Générateurs

Les générateurs (fer, or, diamant, émeraude) sont des tâches déclenchées toutes les 20 ticks lorsque l'arène est en état `RUNNING`. Les délais et quantités proviennent de `config.yml` et peuvent évoluer selon les améliorations.

## Réinitialisation de carte

Par défaut, l'arène est rechargée à partir d'une copie de son monde lorsque la partie se termine. Si le plugin [SlimeWorldManager](https://github.com/Grinderwolf/Slime-World-Manager) est présent, un adaptateur spécialisé est utilisé pour accélérer le reset.

