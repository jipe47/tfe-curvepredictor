tfe-curvepredictor
==================

Ce répertoire contient tous les fichiers sources de l'application Android Curve Predictor. Il va de paire avec celui qui entrepose le serveur de jeu : [Curve Manager](https://github.com/jipe47/tfe-curvemanager "Curve Manager").

Description des packages
------------------------

* **activities** : toutes les activités du jeu
* **curve** : toutes les classes permettant de représenter une série temporelle et une zone à prédire.
* **curveviewer** : classes nécessaires à la vue CurveView, avec l'implémentation de prédicateurs
* **grid** : classes pour dessiner des grilles (plusieurs types de grilles étaient prévues mais ce fut abandonné)
* **homeview** : classes pour l'affichage du logo animé sur l'écran d'accueil
* **misc**
	* **enum** : énumérations diverses, notamment celles nécessaires aux modes de jeu et types de prédiction
	* **utils** : pot-pourri de classes aux buts divers et variés.
* **tasks** : ensemble des tâche asynchrones. Elles sont surtout utilisées pour dialoguer avec le serveur de jeu.
	* **containers** : conteneurs pour passer des arguments aux tâches asynchrones
	* **primitives** : conteneurs pour le résultat de l'interprétation du JSON des réponses des tâches asynchrones.
	
	
Licence
-------

Ce code est sous licence [CC BY-NC][cc] : vous avez le droit de réutiliser ce travail en mentionnant son auteur et uniquement pour des utilisations non commerciales.


[cc] : http://en.wikipedia.org/wiki/Creative_Commons_license "Creative Commons licence"
