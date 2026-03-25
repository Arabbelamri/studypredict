from os import getenv
from uuid import uuid4

from fastapi import FastAPI
from pydantic import BaseModel, ConfigDict, Field


class PredictRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    period_days: int = Field(ge=1, le=60)
    hours_worked: float = Field(ge=0, le=500)
    exercises_done: int = Field(ge=0, le=10000)
    sleep_hours_avg: float = Field(ge=0, le=24)


class PredictResponse(BaseModel):
    request_id: str
    model_version: str
    success_percent: int = Field(ge=0, le=100)


app = FastAPI(title="Student Success ML Service", version="1.0.0")
MODEL_VERSION = getenv("MODEL_VERSION", "dev-heuristic-v1")


def heuristic_score(payload: PredictRequest) -> int:
    hours_component = min(payload.hours_worked / max(payload.period_days, 1) * 8.0, 40.0)
    exercises_component = min(payload.exercises_done * 2.0, 35.0)
    sleep_distance = abs(payload.sleep_hours_avg - 7.5)
    sleep_component = max(0.0, 25.0 - sleep_distance * 6.0)
    score = round(hours_component + exercises_component + sleep_component)
    return max(0, min(100, score))


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok", "model_version": MODEL_VERSION}


@app.post("/predict", response_model=PredictResponse)
async def predict(payload: PredictRequest) -> PredictResponse:
    return PredictResponse(
        request_id=str(uuid4()),
        model_version=MODEL_VERSION,
        success_percent=heuristic_score(payload),
    )
