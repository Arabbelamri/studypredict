from sqlalchemy import Float, ForeignKey, Integer, String
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
