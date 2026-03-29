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

## Prérequis

- [Docker](https://www.docker.com) 20+ avec Docker Compose
- Android Studio (4.2+ recommandé) avec un émulateur Pixel ou un appareil connecté
- Java 17 pour compiler l’application Kotlin

## Backend API (Docker)

1. Positionnez-vous dans la racine du projet et construisez les images :  
   ```bash
   docker compose up --build
   ```
2. L’API FastAPI démarre sur `http://localhost:8080`. Elle expose notamment `/v1/notes`, `/v1/notes/voice` et `/v1/notes/{id}/audio`.
3. Le dossier `backend/api/voice_notes` est monté dans le conteneur pour conserver les fichiers audio téléchargés : ne supprimez donc pas les `.m4a` qui s’y accumulent.

## Base de données et migration

- Le backend utilise SQLite (`backend/api/AppliMobile.db`). Lors du premier démarrage après la mise à jour, le service migre automatiquement la table legacy `notes` vers les nouvelles tables `text_notes` et `voice_notes`.
- Aucun script manuel n’est requis : la migration copie chaque ligne, transfère les champs `audio_path` vers `voice_notes` puis supprime l’ancienne table.
- Pour forcer un nouvel import (par exemple si la table legacy existe encore), supprimez `text_notes`, `voice_notes` et `notes` de la base puis relancez `docker compose up --build`.

## Application mobile Android

1. Ouvrez le projet dans Android Studio, laissez Gradle se synchroniser.
2. L’URL du backend est fournie par `buildConfigField("CUSTOM_BACKEND_URL", "...")` dans `app/build.gradle.kts`. Si vous utilisez l’émulateur Android, vous pouvez la laisser vide (le client utilise `10.0.2.2`). Pour un appareil réel, définissez l’IP de votre machine ou utilisez `adb reverse`.
3. Compilez et lancez l’app via :
   ```bash
   ./gradlew assembleDebug
   ```
   Puis installez le `.apk` sur l’émulateur/appareil.
4. Pour utiliser les notes vocales, donnez la permission audio à l’app et cliquez sur « Ajouter le mémo vocal ». Un enregistrement bloqué (appel API) ne doit plus bloquer l’interface.

## Notes vocales

- Chaque mémo vocal est stocké dans `backend/api/voice_notes` côté serveur. Les notes texte restent dans `text_notes` et n’affichent plus de contrôles audio.
- Le flux d’upload protège maintenant l’UI (chargement + try/finally) et le fallback HTTP côté client évite les timeouts longs lorsque le backend personnalisé est indisponible.
- Si vous ajoutez une note texte, la section « Lire » et l’icône ne sont plus visibles dans la liste ; seuls les mémo vocaux montrent les controls audio.

---  
Ce README couvre l’ensemble des étapes pour cloner, construire et lancer le backend + l’app Android sans erreurs. Il reste à jour pour la version StudyPredict V2.0.1.
