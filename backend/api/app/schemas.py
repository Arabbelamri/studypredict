from datetime import datetime
from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, EmailStr, Field


class StrictModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class HealthResponse(StrictModel):
    status: str
    time_utc: datetime


class RegisterRequest(StrictModel):
    email: EmailStr
    password: str = Field(min_length=8, max_length=72)
    display_name: str | None = Field(default=None, min_length=1, max_length=50)


class LoginRequest(StrictModel):
    email: EmailStr
    password: str = Field(min_length=1, max_length=72)


class RefreshRequest(StrictModel):
    refresh_token: str = Field(min_length=10, max_length=2048)


class LogoutRequest(StrictModel):
    refresh_token: str = Field(min_length=10, max_length=2048)


class TokensResponse(StrictModel):
    token_type: str = "Bearer"
    access_token: str
    expires_in: int
    refresh_token: str


class UserPublic(StrictModel):
    id: str
    email: EmailStr
    display_name: str | None = None
    created_at: datetime


class PredictSuccessRequest(StrictModel):
    period_days: int = Field(ge=1, le=60)
    hours_worked: float = Field(ge=0, le=500)
    exercises_done: int = Field(ge=0, le=10000)
    sleep_hours_avg: float = Field(ge=0, le=24)
    attendance: float | None = Field(default=None, ge=0, le=100)
    previous_scores: float | None = Field(default=None, ge=0, le=100)
    tutoring_sessions: int | None = Field(default=None, ge=0, le=10000)
    physical_activity: float | None = Field(default=None, ge=0, le=24)
    extracurricular_activities: bool = False


class PredictSuccessResponse(StrictModel):
    request_id: UUID
    model_version: str
    success_percent: int = Field(ge=0, le=100)
    tips: list["AdviceItem"] = Field(default_factory=list)


class PredictionHistoryItem(StrictModel):
    id: int
    user_id: int
    predicted_score: float
    hours_studied: float
    attendance: float
    grade: str | None = None
    created_at: datetime


class NoteCreateRequest(StrictModel):
    note_type: str = Field(default="text", min_length=3, max_length=20)
    title: str = Field(min_length=1, max_length=120)
    content: str = Field(min_length=1, max_length=4000)


class NoteItem(StrictModel):
    id: int
    note_type: str
    title: str
    content: str
    audio_url: str | None = None
    created_at: datetime
    updated_at: datetime


class AdviceItem(StrictModel):
    id: int
    title: str
    description: str
    category: str


class UserBadgeItem(StrictModel):
    id: int
    badge_name: str
    description: str | None = None
    unlocked: bool
    unlocked_at: datetime | None = None


class ErrorResponse(StrictModel):
    code: str
    message: str
    details: dict[str, Any] | None = None
