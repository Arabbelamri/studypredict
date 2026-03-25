from functools import lru_cache
from pathlib import Path
from uuid import uuid4

import joblib
import pandas as pd

from .config import settings
from .schemas import PredictSuccessRequest, PredictSuccessResponse


COLUMN_MAP = {
    "Hours_Studied": "0",
    "Attendance": "1",
    "Sleep_Hours": "2",
    "Previous_Scores": "3",
    "Extracurricular_Activities": "4",
    "Tutoring_Sessions": "5",
    "Physical_Activity": "6",
}


@lru_cache(maxsize=1)
def _load_model_artifacts() -> tuple[object, list[str]]:
    model_path = Path(settings.ML_MODEL_PATH)
    features_path = Path(settings.ML_FEATURE_COLUMNS_PATH)
    if not model_path.exists():
        raise RuntimeError(f"Model file not found: {model_path}")
    if not features_path.exists():
        raise RuntimeError(f"Feature columns file not found: {features_path}")

    model = joblib.load(model_path)
    feature_columns = joblib.load(features_path)
    return model, [str(column) for column in feature_columns]


async def predict_success(
    payload: PredictSuccessRequest,
    *,
    attendance: float,
    previous_scores: float,
    tutoring_sessions: int,
    physical_activity: float,
) -> PredictSuccessResponse:
    model, feature_columns = _load_model_artifacts()

    features = {
        "Hours_Studied": payload.hours_worked,
        "Attendance": attendance,
        "Sleep_Hours": payload.sleep_hours_avg,
        "Previous_Scores": previous_scores,
        "Extracurricular_Activities": int(payload.extracurricular_activities),
        "Tutoring_Sessions": tutoring_sessions,
        "Physical_Activity": physical_activity,
    }
    mapped = {COLUMN_MAP[name]: value for name, value in features.items()}
    dataframe = pd.DataFrame([mapped]).reindex(columns=feature_columns, fill_value=0)

    prediction = float(model.predict(dataframe)[0])
    success_percent = int(max(0, min(100, round(prediction))))
    return PredictSuccessResponse(
        request_id=uuid4(),
        model_version=settings.ML_MODEL_VERSION,
        success_percent=success_percent,
    )
