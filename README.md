# StudyPredict

Projet Mobile DevOps – Université du Mans

## Description

StudyPredict est une application Android qui aide les étudiants à estimer leur probabilité de réussite académique à partir de leurs habitudes de travail.

L'application collecte plusieurs informations comme :

- le nombre d’heures d’étude par semaine  
- le taux de présence en cours  
- le nombre d’exercices réalisés  
- les heures de sommeil  
- le niveau de concentration  

Ces données sont utilisées pour calculer un *score de réussite* et proposer des *conseils personnalisés* afin d'aider l'étudiant à améliorer ses performances académiques.

Ce projet est réalisé dans le cadre du *module Mobile DevOps à l’Université du Mans*.

## Architecture

Le projet est composé de trois parties principales :

- *Application mobile Android*  
  Développée en Kotlin. Elle permet l’interaction avec l’utilisateur et l’affichage des résultats.

- *API REST (Backend)*  
  Développée en Python avec FastAPI. Elle gère les requêtes de prédiction et communique avec le modèle de Machine Learning.

- *Service de prédiction Machine Learning*  
  Un modèle entraîné permettant d'estimer la probabilité de réussite académique à partir des données fournies par l'utilisateur.

## Fonctionnalités

- Estimation du potentiel de réussite académique
- Affichage d’un score et d’une note globale
- Conseils personnalisés pour améliorer ses habitudes d’étude
- Historique des analyses
- Système de badges
- Rappels de travail
- Notes et mémos
- Recherche de bibliothèques proches (OpenStreetMap)

## Technologies utilisées

### Frontend
- Kotlin
- Android Studio
- Jetpack Compose
- OSMDroid (carte)

### Backend
- Python
- FastAPI

### Machine Learning
- AutoML (modèle entraîné au semestre 1)

### DevOps
- Git
- Docker

## Installation

Cloner le projet :

```bash
git clone https://github.com/Arabbelamri/studypredict.git