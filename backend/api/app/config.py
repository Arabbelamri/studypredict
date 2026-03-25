from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


def _default_database_url() -> str:
    local_db = Path.cwd() / "AppliMobile.db"
    if local_db.exists():
        return f"sqlite:///{local_db.as_posix()}"

    downloads_db = Path.home() / "Downloads" / "AppliMobile.db"
    if downloads_db.exists():
        return f"sqlite:///{downloads_db.as_posix()}"

    return "sqlite:///./AppliMobile.db"


def _discover_ml_artifact(filename: str) -> str:
    current_file = Path(__file__).resolve()
    candidate_bases = [Path.cwd(), current_file.parent, *current_file.parents, Path("/app")]
    seen: set[str] = set()
    for base in candidate_bases:
        key = str(base)
        if key in seen:
            continue
        seen.add(key)
        candidate = base / "ml" / filename
        if candidate.exists():
            return str(candidate)
    return str(Path("/app/ml") / filename)


def _default_ml_model_path() -> str:
    return _discover_ml_artifact("model.pkl")


def _default_feature_columns_path() -> str:
    return _discover_ml_artifact("feature_columns.pkl")


class Settings(BaseSettings):
    model_config = SettingsConfigDict(case_sensitive=True, extra="ignore")

    APP_NAME: str = "Student Success Predictor API"
    APP_HOST: str = "0.0.0.0"
    APP_PORT: int = 8080
    JWT_SECRET: str = "change-me"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_SECONDS: int = 3600
    ML_MODEL_PATH: str = _default_ml_model_path()
    ML_FEATURE_COLUMNS_PATH: str = _default_feature_columns_path()
    ML_MODEL_VERSION: str = "automl-local-v1"
    DATABASE_URL: str = _default_database_url()


settings = Settings()
