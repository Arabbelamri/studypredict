from datetime import UTC, datetime

from fastapi import Depends, Header, HTTPException, status
from sqlalchemy.orm import Session

from .database import SessionLocal
from .models import User
from .schemas import UserPublic
from .security import decode_access_token


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _parse_created_at(value: str) -> datetime:
    parsed = datetime.fromisoformat(value)
    return parsed if parsed.tzinfo else parsed.replace(tzinfo=UTC)


def get_current_user(
    authorization: str | None = Header(default=None),
    db: Session = Depends(get_db),
) -> UserPublic:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Token manquant ou invalide."},
        )

    token = authorization.removeprefix("Bearer ").strip()
    user_id = decode_access_token(token)
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Token manquant ou invalide."},
        )

    try:
        parsed_user_id = int(user_id)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Token manquant ou invalide."},
        ) from None

    user = db.get(User, parsed_user_id)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHORIZED", "message": "Token manquant ou invalide."},
        )

    return UserPublic(
        id=str(user.id),
        email=user.email,
        display_name=None,
        created_at=_parse_created_at(user.created_at),
    )
