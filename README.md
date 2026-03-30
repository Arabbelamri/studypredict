# 🧠 StudyPredict

Un projet complet pour prédire le succès scolaire d’un étudiant : une **app Android Jetpack Compose**, une **API FastAPI** avec authentification, conseils et notes vocales, et des **artéfacts ML** (et service heuristique) pour les prédictions.

---

## Table des matières
1. [Vue d’ensemble](#vue-densemble)
2. [Prérequis](#pré requis)
3. [Installation initiale](#installation-initiale)
4. [Services principaux](#services-principaux)
   - [API FastAPI](#api-fastapi)
   - [Micro-service ML heuristique](#micro-service-ml-heuristique)
   - [App Android](#app-android)
5. [Docker Compose](#docker-compose)
6. [Points d’API essentiels](#points-dapi-essentiels)
7. [ML : régénérer/refaire le modèle](#ml--régénérerrefaire-le-modèle)
8. [Dépannage rapide](#dépannage-rapide)

---

## Vue d’ensemble

Le dépôt regroupe trois volets :
- `app/` : application Android en Jetpack Compose utilisant Compose BOM, navigation, icônes FontAwesome, OpenStreetMap, GPS et OkHttp pour appeler l’API.
- `backend/api/` : API FastAPI empaquetée avec SQLAlchemy, JWT, cryptographie, stockage SQLite (`AppliMobile.db`), upload audio et règles de conseils/badges.
- `backend/ml_service/` : micro-service FastAPI simple (heuristique) pour tester un endpoint `/predict` ainsi qu’un `ml/` avec les artéfacts `model.pkl` et `feature_columns.pkl`.

Ce README décrit comment cloner le projet, installer les dépendances, lancer chaque service sans erreur et faire communiquer l’app mobile avec l’API.

---

## Pré requis

- **Git** (pour cloner).
- **Java 17+ et Android SDK** (Android Studio recommandé). Le projet utilise AGP 8.6 et Kotlin 1.9.25.
- **Gradle Wrapper** : aucun install global requis, on utilise `./gradlew`.
- **Python 3.11** (pour l’API et le service heuristique).
- **pip** (installer les dépendances listées dans `backend/api/requirements.txt` et `backend/ml_service/requirements.txt`).
- **Docker + Docker Compose** (optionnel mais recommandé pour l’API).
- **Ports libres 8080/8081** (ou adaptez les variables d’environnement).

---

## Installation initiale

```bash
git clone <url_du_repo>
cd studypredict
```

1. Vérifiez que les dossiers `ml/`, `backend/api/voice_notes/` et `backend/api/AppliMobile.db` sont présents (ils le sont par défaut). Sinon, copiez les artéfacts `model.pkl` et `feature_columns.pkl` dans `ml/`.
2. Créez un dossier `backend/api/voice_notes/` si absent : `mkdir -p backend/api/voice_notes`.
3. À chaque fois que vous modifiez le modèle ML, régénérez les fichiers décrits plus bas (section [ML : régénérer/refaire le modèle](#ml--régénérerrefaire-le-modèle)).

---

## Services principaux

### API FastAPI

#### Installation

```bash
cd backend/api
python -m venv .venv
.venv\Scripts\activate     # sous Windows, ou `source .venv/bin/activate` sous Unix
pip install --upgrade pip
pip install -r requirements.txt
```

#### Configuration (variables d’environnement)

| Variable | Valeur par défaut | Usage |
| --- | --- | --- |
| `APP_NAME` | `Student Success Predictor API` | Nom affiché dans la doc FastAPI |
| `APP_HOST` | `0.0.0.0` | Host sur lequel uvicorn écoute |
| `APP_PORT` | `8080` | Port de l’API |
| `JWT_SECRET` | `change-me` | Changez la valeur en production |
| `ACCESS_TOKEN_EXPIRE_SECONDS` | `3600` | Durée de vie du token |
| `DATABASE_URL` | Recherche `AppliMobile.db`, sinon `./AppliMobile.db` | Attention au chemin absolu |
| `VOICE_NOTES_DIR` | `./voice_notes` | Répertoire d’upload des notes vocales |
| `ML_MODEL_PATH` / `ML_FEATURE_COLUMNS_PATH` | Recherche `ml/model.pkl` | Pointent vers le modèle et les colonnes joblib |
| `ML_MODEL_VERSION` | `automl-local-v1` | Version renvoyée dans les réponses |

Vous pouvez créer un fichier `.env` (avec `python-dotenv` si besoin) ou exporter les vars avant le lancement.

#### Démarrage

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8080
```

L’API est maintenant disponible sur `http://localhost:8080`, avec la documentation interactive sur `/docs` ou `/redoc`.

#### Notes

- `AppliMobile.db` contient déjà des tables pré-remplies (utilisateurs, prédictions). La migration `_migrate_notes_table` se déclenche au démarrage.
- Les notes vocales sont conservées dans `backend/api/voice_notes`. Le service vérifie que le fichier audio existe et expose `/v1/notes/{id}/audio`.
- Les tokens de rafraîchissement sont stockés en mémoire (`store.refresh_tokens`).

### Micro-service ML heuristique

Ce service est jardin supplémentaire (et indépendant de `ml/model.pkl`) pour tester un endpoint `/predict`.

```bash
cd backend/ml_service
python -m venv .venv
.venv\Scripts\activate
pip install --upgrade pip
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8081
```

Envoyez une requête `POST /predict` avec le schéma `PredictRequest` (contenant `period_days`, `hours_worked`, `exercises_done`, `sleep_hours_avg`). Le service renvoie un `success_percent` dérivé d’une heuristique et un `request_id` pour débogage. Utilisez-le uniquement en test ou pour isolement.

### App Android

1. Ouvrez le projet dans **Android Studio** ou utilisez le wrapper :
   ```bash
   ./gradlew clean installDebug
   ```
2. Le champ `CUSTOM_BACKEND_URL` dans `app/build.gradle.kts` pointe actuellement vers `http://10.98.151.155:8080`. Adaptez-le à :
   - `http://10.0.2.2:8080` pour un émulateur Android standard.
   - `http://<IP_MAQUINE>:8080` pour un appareil physique (100% accessible sur votre LAN).
3. Assurez-vous que l’API tourne/que les ports sont atteignables avant de lancer l’app.

L’app utilise :
   - Compose (material3, navigation, icônes, OSM et GPS).
   - OkHttp pour consommer les endpoints `/v1/auth/*`, `/v1/predict-success`, `/v1/notes`, `/v1/tips/me/latest`, `/v1/badges/me`.

#### Tester sur un téléphone via Wi-Fi

1. **Identifier l’IP locale de la machine qui héberge l’API**  
   - Sous Windows: `ipconfig` ➜ cherchez l’“IPv4” du réseau Wi-Fi ou Ethernet.  
   - Sous macOS/Linux: `ifconfig` ou `hostname -I`.
2. **Adapter `CUSTOM_BACKEND_URL`** dans `app/build.gradle.kts` ou via un fichier de configuration que vous injectez à la build :
   - Exemple pour un appareil physique connecté au même réseau Wi-Fi: `http://192.168.1.42:8080`.
   - Si le téléphone utilise un VPN ou un sous-réseau différent, assurez-vous que la route existe et que la machine est joignable sur ce réseau (ping, `curl` depuis un autre appareil).
3. **Ouvrir le port 8080** (ou le port utilisé) sur la machine/routeur:
   - Désactivez temporairement les pare-feux qui bloquent `uvicorn`, ou ajoutez une règle entrante autorisant `8080`.
   - Sur certains routeurs, activez la fonction “AP Isolation” ou créez un port-forward si vous testez en dehors du LAN.
4. **Lancer l’API avant de démarrer l’app** (voir section [API FastAPI](#api-fastapi)).  
   - Vérifiez que `curl http://<IP>:8080/v1/health` renvoie `{"status":"ok"}` avant de lancer l’app.

> Pour revenir à un émulateur standard, replacez `CUSTOM_BACKEND_URL` par `http://10.0.2.2:8080`.

---

## Docker Compose

Le fichier `docker-compose.yml` containerise uniquement l’API (utile pour environnement de test rapide) :

```bash
docker compose up --build
```

Volumes montés :
- `./backend/api/AppliMobile.db:/app/AppliMobile.db` pour persister la base.
- `./backend/api/voice_notes:/app/voice_notes`.

Environnement automatiquement injecté : jetons, base SQLite, chemins ML, etc. Le répertoire `ml/` à la racine doit contenir `model.pkl` et `feature_columns.pkl` avant `docker compose up`.

> Pour arrêter : `docker compose down`.

---

## Points d’API essentiels

- `POST /v1/auth/register` : inscription (email unique).  
- `POST /v1/auth/login` : renvoie `access_token`, `refresh_token`.  
- `POST /v1/auth/refresh` et `POST /v1/auth/logout` : gestion des tokens.  
- `GET /v1/users/me` : profil (JWT requis).  
- `POST /v1/predict-success` : envoie les indicateurs du jour, reçoit `success_percent`, `tips` (qui sont enregistrés et donnent badges).  
- `GET /v1/predictions` : historique de notes.  
- `/v1/notes` (GET/POST/DELETE) et `/v1/notes/voice` (upload audio).  
- `/v1/notes/{id}/audio` : téléchargement sécurisé d’une note vocale.  
- `/v1/tips/me/latest` et `/v1/badges/me` : conseils et badges calculés à partir des règles définies dans `_ensure_advice_catalog` et `_compute_prediction_badges`.

Le contrat OpenAPI se trouve dans `student-success-api-contract (2).yaml`.

---

## ML : régénérer/refaire le modèle

1. Le dossier `ml/` contient :
   - `dataset_clean.data` / `.solution` (jeu de données compressé).
   - `model.pkl` et `feature_columns.pkl` utilisés par l’API et le Dockerfile.
2. Pour régénérer :
   ```bash
   cd ml
   python train_model.py
   ```
   > `train_model.py` dépend du module `automl_group13` (il attend `automl/automl.py`). Assurez-vous qu’il est dans `PYTHONPATH` (ajoutez ou installez via pip si nécessaire).
3. Test rapide :
   ```bash
   python test_predict.py
   ```
   Vérifie que `model.pkl` accepte les colonnes `COLUMN_MAP`.
4. Copiez ensuite `model.pkl` et `feature_columns.pkl` dans :
   - `ml/` (déjà là).
   - `backend/api/ml/` si vous utilisez Docker (le Dockerfile fait `COPY ml/model.pkl ...` et `ml/feature_columns.pkl ...`).

---

## Dépannage rapide

- **L’API ne démarre pas** : vérifiez `DATABASE_URL` et que `AppliMobile.db` est accessible. Effacez `backend/api/.venv` et réinstallez si packages corrompus.
- **Le modèle joblib est introuvable** : assurez-vous que `ml/model.pkl` et `ml/feature_columns.pkl` existent. Pour Docker, `ml/` doit être au même niveau que `backend/`.
- **Authentification rejetée** : utilisez les bons tokens (`Authorization: Bearer <token>`). Les refresh tokens sont conservés en mémoire (`backend/api/app/store.py`).
- **Upload audio échoue** : créez le dossier `VOICE_NOTES_DIR` (`backend/api/voice_notes` par défaut). Vérifiez les permissions (le service écrit avec l’utilisateur de l’API).
- **App Android ne trouve pas l’API** : utilisez l’adresse IP de votre machine (ou 10.0.2.2 pour émulateur). Vérifiez que des antivirus ou firewalls ne bloquent pas le port 8080.
- **Docker Compose échoue** : supprimez les volumes `AppliMobile.db`/`voice_notes`, rebuild (`docker compose up --force-recreate --build`).

---

## Prochaine étape

1. Lancer les tests unitaires Android : `./gradlew test`.
2. Ajouter de nouveaux conseils ou badges dans `backend/api/app/main.py`.
3. Connecter une base de données PostgreSQL en changeant `DATABASE_URL` (l’API fonctionne avec n’importe quelle URL SQLAlchemy).

