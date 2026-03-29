from datetime import UTC, datetime
import mimetypes
import shutil
from pathlib import Path
from uuid import uuid4

from fastapi import Depends, FastAPI, File, Form, HTTPException, Request, UploadFile, status
from fastapi.responses import FileResponse
from sqlalchemy import func
from sqlalchemy.orm import Session

from .config import settings
from .database import engine
from .dependencies import get_current_user, get_db
from .ml_client import predict_success
from .models import (
    Advice,
    AdviceRule,
    Badge,
    Base,
    Note,
    Prediction,
    PredictionAdvice,
    User,
    UserBadge,
)
from .schemas import (
    AdviceItem,
    HealthResponse,
    LoginRequest,
    LogoutRequest,
    NoteCreateRequest,
    NoteItem,
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

ADVICE_DEFINITIONS = [
    (
        "Sommeil en priorité",
        "Vous dormez peu. Essayez d'atteindre 7h minimum pour mieux mémoriser.",
        "sleep",
        {
            "max_sleep_hours": 6.5,
            "priority": 10,
        },
    ),
    (
        "Améliorez votre assiduité",
        "Votre présence est faible. Visez au moins 85% pour progresser plus vite.",
        "attendance",
        {
            "max_attendance": 74.99,
            "priority": 20,
        },
    ),
    (
        "Pratiquez davantage",
        "Le nombre d'exercices est faible. Faites des sessions régulières chaque semaine.",
        "practice",
        {
            "max_exercises_done": 7,
            "priority": 30,
        },
    ),
    (
        "Consolider les bases",
        "Le score prédit est encore fragile. Renforcez les fondamentaux sur 2 semaines.",
        "score",
        {
            "max_success_percent": 59.99,
            "priority": 40,
        },
    ),
    (
        "Bon rythme à maintenir",
        "Vos indicateurs sont bons. Continuez avec une routine stable.",
        "consistency",
        {
            "min_success_percent": 60,
            "min_sleep_hours": 7,
            "min_attendance": 80,
            "min_exercises_done": 8,
            "priority": 50,
        },
    ),
    (
        "Excellent profil",
        "Très bon potentiel. Maintenez cette discipline et ajoutez des objectifs avancés.",
        "excellence",
        {
            "min_success_percent": 80,
            "min_sleep_hours": 7,
            "min_attendance": 85,
            "min_exercises_done": 12,
            "priority": 5,
        },
    ),
    (
        "Conseil général",
        "Restez régulier: planification hebdomadaire, sommeil stable, et entraînement actif.",
        "general",
        {
            "priority": 999,
        },
    ),
]


def _parse_optional_datetime(value: str | None) -> datetime | None:
    if not value:
        return None
    try:
        parsed = datetime.fromisoformat(value)
        return parsed if parsed.tzinfo else parsed.replace(tzinfo=UTC)
    except ValueError:
        return None


def _note_audio_url(note: Note, request: Request | None) -> str | None:
    if not note.audio_path or request is None:
        return None
    return request.url_for("download_voice_note", note_id=note.id)


def _to_note_item(note: Note, request: Request | None) -> NoteItem:
    return NoteItem(
        id=note.id,
        note_type=note.note_type,
        title=note.title,
        content=note.content,
        audio_url=_note_audio_url(note, request),
        created_at=_parse_created_at(note.created_at),
        updated_at=_parse_created_at(note.updated_at),
    )


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


def _ensure_advice_catalog(db: Session) -> tuple[dict[str, Advice], list[AdviceRule]]:
    catalog: dict[str, Advice] = {}
    for advice in db.query(Advice).all():
        catalog[advice.title] = advice

    next_advice_id = (db.query(func.max(Advice.id)).scalar() or 0) + 1
    next_rule_id = (db.query(func.max(AdviceRule.id)).scalar() or 0) + 1

    for title, description, category, _conditions in ADVICE_DEFINITIONS:
        advice = catalog.get(title)
        if advice is None:
            advice = Advice(
                id=next_advice_id,
                title=title,
                description=description,
                category=category,
                is_active=True,
            )
            db.add(advice)
            db.flush()
            catalog[title] = advice
            next_advice_id += 1

    existing_rules = db.query(AdviceRule).all()
    if not existing_rules:
        for title, _description, _category, conditions in ADVICE_DEFINITIONS:
            advice = catalog[title]
            db.add(
                AdviceRule(
                    id=next_rule_id,
                    advice_id=advice.id,
                    priority=int(conditions.get("priority", 100)),
                    min_success_percent=conditions.get("min_success_percent"),
                    max_success_percent=conditions.get("max_success_percent"),
                    min_attendance=conditions.get("min_attendance"),
                    max_attendance=conditions.get("max_attendance"),
                    min_sleep_hours=conditions.get("min_sleep_hours"),
                    max_sleep_hours=conditions.get("max_sleep_hours"),
                    min_exercises_done=conditions.get("min_exercises_done"),
                    max_exercises_done=conditions.get("max_exercises_done"),
                )
            )
            next_rule_id += 1
        db.flush()
        existing_rules = db.query(AdviceRule).all()

    return catalog, existing_rules


def _matches_rule(
    *,
    rule: AdviceRule,
    success_percent: float,
    attendance: float,
    sleep_hours: float,
    exercises_done: int,
) -> bool:
    if rule.min_success_percent is not None and success_percent < rule.min_success_percent:
        return False
    if rule.max_success_percent is not None and success_percent > rule.max_success_percent:
        return False
    if rule.min_attendance is not None and attendance < rule.min_attendance:
        return False
    if rule.max_attendance is not None and attendance > rule.max_attendance:
        return False
    if rule.min_sleep_hours is not None and sleep_hours < rule.min_sleep_hours:
        return False
    if rule.max_sleep_hours is not None and sleep_hours > rule.max_sleep_hours:
        return False
    if rule.min_exercises_done is not None and exercises_done < rule.min_exercises_done:
        return False
    if rule.max_exercises_done is not None and exercises_done > rule.max_exercises_done:
        return False
    return True


def _compute_prediction_advices(
    *,
    rules: list[AdviceRule],
    advice_catalog: dict[int, Advice],
    success_percent: float,
    attendance: float,
    sleep_hours: float,
    exercises_done: int,
    max_items: int = 3,
) -> list[Advice]:
    matching = [
        rule
        for rule in rules
        if _matches_rule(
            rule=rule,
            success_percent=success_percent,
            attendance=attendance,
            sleep_hours=sleep_hours,
            exercises_done=exercises_done,
        )
    ]
    ordered = sorted(matching, key=lambda rule: rule.priority)

    selected: list[Advice] = []
    seen_ids: set[int] = set()
    for rule in ordered:
        advice = advice_catalog.get(rule.advice_id)
        if advice is None or not advice.is_active or advice.id in seen_ids:
            continue
        selected.append(advice)
        seen_ids.add(advice.id)
        if len(selected) >= max_items:
            break
    return selected


def _persist_prediction_advices(
    *,
    db: Session,
    prediction_id: int,
    advices: list[Advice],
) -> None:
    for advice in advices:
        exists = (
            db.query(PredictionAdvice)
            .filter(
                PredictionAdvice.prediction_id == prediction_id,
                PredictionAdvice.advice_id == advice.id,
            )
            .first()
        )
        if exists is None:
            db.add(PredictionAdvice(prediction_id=prediction_id, advice_id=advice.id))


def _to_advice_item(advice: Advice) -> AdviceItem:
    return AdviceItem(
        id=advice.id,
        title=advice.title,
        description=advice.description,
        category=advice.category,
    )


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
            user_id=prediction.user_id,
            predicted_score=prediction.predicted_score,
            hours_studied=prediction.hours_studied,
            attendance=prediction.attendance,
            grade=prediction.grade,
            created_at=_parse_created_at(prediction.created_at),
        )
        for prediction in predictions
    ]


@app.get("/v1/notes/me", response_model=list[NoteItem], tags=["Notes"])
async def list_my_notes(
    request: Request,
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[NoteItem]:
    notes = (
        db.query(Note)
        .filter(Note.user_id == int(current_user.id))
        .order_by(Note.id.desc())
        .all()
    )
    return [_to_note_item(note, request) for note in notes]


@app.post("/v1/notes", response_model=NoteItem, status_code=status.HTTP_201_CREATED, tags=["Notes"])
async def create_note(
    request: Request,
    payload: NoteCreateRequest,
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> NoteItem:
    now_iso = datetime.now(UTC).isoformat()
    note = Note(
        user_id=int(current_user.id),
        note_type=payload.note_type.strip().lower(),
        title=payload.title.strip(),
        content=payload.content.strip(),
        created_at=now_iso,
        updated_at=now_iso,
    )
    db.add(note)
    db.commit()
    db.refresh(note)

    return _to_note_item(note, request)


@app.post("/v1/notes/voice", response_model=NoteItem, status_code=status.HTTP_201_CREATED, tags=["Notes"])
async def create_voice_note(
    request: Request,
    title: str = Form(..., min_length=1, max_length=120),
    content: str | None = Form(default=None),
    audio_file: UploadFile = File(...),
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> NoteItem:
    if not audio_file.content_type or not audio_file.content_type.startswith("audio/"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={"code": "INVALID_AUDIO_FILE", "message": "Le fichier doit être un audio valide."},
        )

    voice_dir = Path(settings.VOICE_NOTES_DIR)
    voice_dir.mkdir(parents=True, exist_ok=True)
    file_suffix = Path(audio_file.filename or "").suffix or ".m4a"
    target_path = voice_dir / f"{uuid4().hex}{file_suffix}"

    try:
        with target_path.open("wb") as buffer:
            shutil.copyfileobj(audio_file.file, buffer)
    finally:
        await audio_file.close()

    now_iso = datetime.now(UTC).isoformat()
    note = Note(
        user_id=int(current_user.id),
        note_type="voice",
        title=title.strip(),
        content=(content.strip() if content else ""),
        audio_path=str(target_path.resolve()),
        created_at=now_iso,
        updated_at=now_iso,
    )
    db.add(note)
    db.commit()
    db.refresh(note)

    return _to_note_item(note, request)


@app.get(
    "/v1/notes/{note_id}/audio",
    response_class=FileResponse,
    name="download_voice_note",
    tags=["Notes"],
)
async def download_voice_note(
    note_id: int,
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> FileResponse:
    note = (
        db.query(Note)
        .filter(Note.id == note_id, Note.user_id == int(current_user.id))
        .first()
    )
    if note is None or not note.audio_path:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "NOTE_NOT_FOUND", "message": "Note introuvable."},
        )

    audio_path = Path(note.audio_path)
    if not audio_path.exists():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "AUDIO_UNAVAILABLE", "message": "Fichier audio introuvable."},
        )

    media_type, _ = mimetypes.guess_type(audio_path.name)
    return FileResponse(
        path=str(audio_path),
        media_type=media_type or "application/octet-stream",
        filename=audio_path.name,
    )


@app.delete("/v1/notes/{note_id}", status_code=status.HTTP_204_NO_CONTENT, tags=["Notes"])
async def delete_note(
    note_id: int,
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> None:
    note = (
        db.query(Note)
        .filter(Note.id == note_id, Note.user_id == int(current_user.id))
        .first()
    )
    if note is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "NOTE_NOT_FOUND", "message": "Note introuvable."},
        )
    db.delete(note)
    db.commit()


@app.get("/v1/tips/me/latest", response_model=list[AdviceItem], tags=["Tips"])
async def list_latest_tips(
    current_user: UserPublic = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[AdviceItem]:
    latest_prediction = (
        db.query(Prediction)
        .filter(Prediction.user_id == int(current_user.id))
        .order_by(Prediction.id.desc())
        .first()
    )
    if latest_prediction is None:
        return []

    advice_rows = (
        db.query(Advice)
        .join(PredictionAdvice, PredictionAdvice.advice_id == Advice.id)
        .filter(PredictionAdvice.prediction_id == latest_prediction.id)
        .all()
    )

    if not advice_rows:
        catalog_by_name, rules = _ensure_advice_catalog(db)
        catalog_by_id = {advice.id: advice for advice in catalog_by_name.values()}
        advice_rows = _compute_prediction_advices(
            rules=rules,
            advice_catalog=catalog_by_id,
            success_percent=latest_prediction.predicted_score,
            attendance=latest_prediction.attendance,
            sleep_hours=latest_prediction.sleep_hours,
            exercises_done=latest_prediction.tutoring_sessions,
        )
        _persist_prediction_advices(
            db=db,
            prediction_id=latest_prediction.id,
            advices=advice_rows,
        )
        db.commit()

    return [_to_advice_item(advice) for advice in advice_rows]


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
    advice_catalog_by_name, advice_rules = _ensure_advice_catalog(db)
    advice_catalog_by_id = {advice.id: advice for advice in advice_catalog_by_name.values()}
    unlocked_badges = _compute_prediction_badges(
        success_percent=prediction.success_percent,
        attendance=attendance,
        sleep_hours_avg=payload.sleep_hours_avg,
        exercises_done=payload.exercises_done,
    )
    computed_advices = _compute_prediction_advices(
        rules=advice_rules,
        advice_catalog=advice_catalog_by_id,
        success_percent=prediction.success_percent,
        attendance=attendance,
        sleep_hours=payload.sleep_hours_avg,
        exercises_done=payload.exercises_done,
    )

    db.add(record)
    db.flush()
    _persist_prediction_advices(
        db=db,
        prediction_id=record.id,
        advices=computed_advices,
    )
    _award_user_badges(
        db=db,
        user_id=int(current_user.id),
        badge_names=unlocked_badges,
        catalog=catalog,
    )
    db.commit()

    return PredictSuccessResponse(
        request_id=prediction.request_id,
        model_version=prediction.model_version,
        success_percent=prediction.success_percent,
        tips=[_to_advice_item(advice) for advice in computed_advices],
    )
