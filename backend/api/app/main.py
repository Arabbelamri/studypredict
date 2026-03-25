from datetime import UTC, datetime

from fastapi import Depends, FastAPI, HTTPException, status
from sqlalchemy import func
from sqlalchemy.orm import Session

from .config import settings
from .database import engine
from .dependencies import get_current_user, get_db
from .ml_client import predict_success
from .models import Badge, Base, Prediction, User, UserBadge
from .schemas import (
    HealthResponse,
    LoginRequest,
    LogoutRequest,
    PredictionHistoryItem,
    PredictSuccessRequest,
    PredictSuccessResponse,
    RefreshRequest,
    RegisterRequest,
    TokensResponse,
    UserBadgeItem,
    UserPublic,
)
from .security import create_access_token, create_refresh_token, hash_password, verify_password
from .store import store

app = FastAPI(title=settings.APP_NAME, version="1.0.0")
Base.metadata.create_all(bind=engine)


def _parse_created_at(value: str) -> datetime:
    parsed = datetime.fromisoformat(value)
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=UTC)


def _prediction_grade(success_percent: int) -> str:
    if success_percent >= 80:
        return "A"
    if success_percent >= 60:
        return "B"
    if success_percent >= 40:
        return "C"
    return "D"


def _estimate_attendance(payload: PredictSuccessRequest) -> float:
    daily_hours = payload.hours_worked / max(payload.period_days, 1)
    return round(min(100.0, daily_hours * 12.5), 2)


def _estimate_previous_scores(payload: PredictSuccessRequest) -> float:
    practice_factor = min(payload.exercises_done * 5.0, 100.0)
    sleep_bonus = max(0.0, 10.0 - abs(payload.sleep_hours_avg - 7.5) * 4.0)
    return round(min(100.0, practice_factor * 0.7 + sleep_bonus), 2)


BADGE_DEFINITIONS = [
    ("Bon potentiel", "Obtenir un score predit superieur ou egal a 70."),
    ("Assidu", "Atteindre un taux de presence superieur ou egal a 85%."),
    ("\u00c9quilibr\u00e9", "Maintenir au moins 7 heures de sommeil moyen."),
    ("R\u00e9gulier", "Faire au moins 12 exercices sur la periode."),
    ("En progression", "Continuer les efforts meme sans seuil debloque."),
]


def _parse_optional_datetime(value: str | None) -> datetime | None:
    if not value:
        return None
    try:
        parsed = datetime.fromisoformat(value)
        return parsed if parsed.tzinfo else parsed.replace(tzinfo=UTC)
    except ValueError:
        return None


def _ensure_badges_catalog(db: Session) -> dict[str, Badge]:
    catalog: dict[str, Badge] = {}
    existing_badges = db.query(Badge).all()
    for badge in existing_badges:
        catalog[badge.badge_name] = badge

    next_id = (db.query(func.max(Badge.id)).scalar() or 0) + 1
    for name, description in BADGE_DEFINITIONS:
        badge = catalog.get(name)
        if badge is None:
            badge = Badge(id=next_id, badge_name=name, description=description)
            db.add(badge)
            db.flush()
            catalog[name] = badge
            next_id += 1

    return catalog


def _compute_prediction_badges(
    *,
    success_percent: int,
    attendance: float,
    sleep_hours_avg: float,
    exercises_done: int,
) -> list[str]:
    badges: list[str] = []
    if success_percent >= 70:
        badges.append("Bon potentiel")
    if attendance >= 85:
        badges.append("Assidu")
    if sleep_hours_avg >= 7:
        badges.append("\u00c9quilibr\u00e9")
    if exercises_done >= 12:
        badges.append("R\u00e9gulier")
    if not badges:
        badges.append("En progression")
    return badges


def _award_user_badges(
    *,
    db: Session,
    user_id: int,
    badge_names: list[str],
    catalog: dict[str, Badge],
) -> None:
    now_iso = datetime.now(UTC).isoformat()
    for badge_name in badge_names:
        badge = catalog.get(badge_name)
        if badge is None:
            continue
        already_unlocked = (
            db.query(UserBadge)
            .filter(UserBadge.user_id == user_id, UserBadge.badge_id == badge.id)
            .first()
        )
        if already_unlocked is None:
            db.add(
                UserBadge(
                    user_id=user_id,
                    badge_id=badge.id,
                    unlocked_at=now_iso,
                )
            )


@app.get("/v1/health", response_model=HealthResponse, tags=["System"])
async def health() -> HealthResponse:
    return HealthResponse(status="ok", time_utc=datetime.now(UTC))


@app.post("/v1/auth/register", response_model=UserPublic, status_code=status.HTTP_201_CREATED, tags=["Auth"])
async def register(payload: RegisterRequest, db: Session = Depends(get_db)) -> UserPublic:
    email = payload.email.lower()
    existing_user = db.query(User).filter(User.email == email).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail={"code": "EMAIL_ALREADY_USED", "message": "Cet email est deja utilise."},
        )

    user = User(
        email=email,
        password_hash=hash_password(payload.password),
        created_at=datetime.now(UTC).isoformat(),
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    return UserPublic(
        id=str(user.id),
        email=user.email,
        display_name=payload.display_name,
        created_at=_parse_created_at(user.created_at),
    )


@app.post("/v1/auth/login", response_model=TokensResponse, tags=["Auth"])
async def login(payload: LoginRequest, db: Session = Depends(get_db)) -> TokensResponse:
    user = db.query(User).filter(User.email == payload.email.lower()).first()
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Email ou mot de passe invalide."},
        )

    if "$" not in user.password_hash:
        user.password_hash = hash_password(payload.password)
        db.add(user)
        db.commit()

    refresh_token = create_refresh_token()
    store.refresh_tokens[refresh_token] = str(user.id)
    return TokensResponse(
        access_token=create_access_token(str(user.id)),
        expires_in=settings.ACCESS_TOKEN_EXPIRE_SECONDS,
        refresh_token=refresh_token,
    )


@app.post("/v1/auth/refresh", response_model=TokensResponse, tags=["Auth"])
async def refresh(payload: RefreshRequest) -> TokensResponse:
    user_id = store.refresh_tokens.get(payload.refresh_token)
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Refresh token invalide."},
        )

    return TokensResponse(
        access_token=create_access_token(user_id),
        expires_in=settings.ACCESS_TOKEN_EXPIRE_SECONDS,
        refresh_token=payload.refresh_token,
    )


@app.post("/v1/auth/logout", status_code=status.HTTP_204_NO_CONTENT, tags=["Auth"])
async def logout(payload: LogoutRequest) -> None:
    if payload.refresh_token not in store.refresh_tokens:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Refresh token invalide."},
        )
    del store.refresh_tokens[payload.refresh_token]


@app.get("/v1/users/me", response_model=UserPublic, tags=["Users"])
async def users_me(current_user: UserPublic = Depends(get_current_user)) -> UserPublic:
    return current_user


@app.get("/v1/predictions", response_model=list[PredictionHistoryItem], tags=["Prediction"])
async def list_predictions(
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[PredictionHistoryItem]:
    predictions = (
        db.query(Prediction)
        .filter(Prediction.user_id == int(current_user.id))
        .order_by(Prediction.id.desc())
        .all()
    )
    return [
        PredictionHistoryItem(
            id=prediction.id,
            predicted_score=prediction.predicted_score,
            grade=prediction.grade,
            created_at=_parse_created_at(prediction.created_at),
        )
        for prediction in predictions
    ]


@app.get("/v1/badges/me", response_model=list[UserBadgeItem], tags=["Badges"])
async def list_my_badges(
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[UserBadgeItem]:
    catalog = _ensure_badges_catalog(db)
    all_badges = sorted(catalog.values(), key=lambda badge: badge.id)
    unlocked_rows = (
        db.query(UserBadge)
        .filter(UserBadge.user_id == int(current_user.id))
        .all()
    )
    unlocked_map = {row.badge_id: row.unlocked_at for row in unlocked_rows}

    return [
        UserBadgeItem(
            id=badge.id,
            badge_name=badge.badge_name,
            description=badge.description,
            unlocked=badge.id in unlocked_map,
            unlocked_at=_parse_optional_datetime(unlocked_map.get(badge.id)),
        )
        for badge in all_badges
    ]


@app.post("/v1/predict-success", response_model=PredictSuccessResponse, tags=["Prediction"])
async def predict(
    payload: PredictSuccessRequest,
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> PredictSuccessResponse:
    attendance = payload.attendance if payload.attendance is not None else _estimate_attendance(payload)
    previous_scores = payload.previous_scores if payload.previous_scores is not None else _estimate_previous_scores(payload)
    tutoring_sessions = payload.tutoring_sessions if payload.tutoring_sessions is not None else payload.exercises_done
    physical_activity = payload.physical_activity or 0.0

    try:
        prediction = await predict_success(
            payload,
            attendance=attendance,
            previous_scores=previous_scores,
            tutoring_sessions=tutoring_sessions,
            physical_activity=physical_activity,
        )
    except RuntimeError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={"code": "ML_MODEL_UNAVAILABLE", "message": str(exc)},
        ) from exc

    record = Prediction(
        user_id=int(current_user.id),
        hours_studied=payload.hours_worked,
        attendance=attendance,
        sleep_hours=payload.sleep_hours_avg,
        previous_scores=previous_scores,
        tutoring_sessions=tutoring_sessions,
        physical_activity=physical_activity,
        predicted_score=float(prediction.success_percent),
        grade=_prediction_grade(prediction.success_percent),
        created_at=datetime.now(UTC).isoformat(),
        extracurricular_activities=payload.extracurricular_activities,
    )
    catalog = _ensure_badges_catalog(db)
    unlocked_badges = _compute_prediction_badges(
        success_percent=prediction.success_percent,
        attendance=attendance,
        sleep_hours_avg=payload.sleep_hours_avg,
        exercises_done=payload.exercises_done,
    )

    db.add(record)
    _award_user_badges(
        db=db,
        user_id=int(current_user.id),
        badge_names=unlocked_badges,
        catalog=catalog,
    )
    db.commit()

    return prediction
