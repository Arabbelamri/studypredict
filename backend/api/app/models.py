from sqlalchemy import Boolean, Float, ForeignKey, Integer, String
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    email: Mapped[str] = mapped_column(String, unique=True, index=True)
    password_hash: Mapped[str] = mapped_column(String)
    created_at: Mapped[str] = mapped_column(String)


class Prediction(Base):
    __tablename__ = "predictions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True)
    hours_studied: Mapped[float] = mapped_column(Float)
    attendance: Mapped[float] = mapped_column(Float)
    sleep_hours: Mapped[float] = mapped_column(Float)
    previous_scores: Mapped[float] = mapped_column(Float)
    tutoring_sessions: Mapped[int] = mapped_column(Integer)
    physical_activity: Mapped[float] = mapped_column(Float)
    predicted_score: Mapped[float] = mapped_column(Float)
    grade: Mapped[str | None] = mapped_column(String, nullable=True)
    created_at: Mapped[str] = mapped_column(String)
    extracurricular_activities: Mapped[bool] = mapped_column("Extracurricular_Activities")


class Badge(Base):
    __tablename__ = "badges"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    badge_name: Mapped[str] = mapped_column(String)
    description: Mapped[str | None] = mapped_column(String, nullable=True)


class UserBadge(Base):
    __tablename__ = "user_badges"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True)
    badge_id: Mapped[int] = mapped_column(ForeignKey("badges.id"), index=True)
    unlocked_at: Mapped[str] = mapped_column(String)


class TextNote(Base):
    __tablename__ = "text_notes"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True)
    title: Mapped[str] = mapped_column(String)
    content: Mapped[str] = mapped_column(String)
    created_at: Mapped[str] = mapped_column(String)
    updated_at: Mapped[str] = mapped_column(String)


class VoiceNote(Base):
    __tablename__ = "voice_notes"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"), index=True)
    title: Mapped[str] = mapped_column(String)
    content: Mapped[str] = mapped_column(String)
    audio_path: Mapped[str | None] = mapped_column(String, nullable=True)
    created_at: Mapped[str] = mapped_column(String)
    updated_at: Mapped[str] = mapped_column(String)


class Advice(Base):
    __tablename__ = "advices"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    title: Mapped[str] = mapped_column(String)
    description: Mapped[str] = mapped_column(String)
    category: Mapped[str] = mapped_column(String, index=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)


class AdviceRule(Base):
    __tablename__ = "advice_rules"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    advice_id: Mapped[int] = mapped_column(ForeignKey("advices.id"), index=True)
    priority: Mapped[int] = mapped_column(Integer, default=100)
    min_success_percent: Mapped[float | None] = mapped_column(Float, nullable=True)
    max_success_percent: Mapped[float | None] = mapped_column(Float, nullable=True)
    min_attendance: Mapped[float | None] = mapped_column(Float, nullable=True)
    max_attendance: Mapped[float | None] = mapped_column(Float, nullable=True)
    min_sleep_hours: Mapped[float | None] = mapped_column(Float, nullable=True)
    max_sleep_hours: Mapped[float | None] = mapped_column(Float, nullable=True)
    min_exercises_done: Mapped[int | None] = mapped_column(Integer, nullable=True)
    max_exercises_done: Mapped[int | None] = mapped_column(Integer, nullable=True)


class PredictionAdvice(Base):
    __tablename__ = "prediction_advices"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    prediction_id: Mapped[int] = mapped_column(ForeignKey("predictions.id"), index=True)
    advice_id: Mapped[int] = mapped_column(ForeignKey("advices.id"), index=True)
