from base64 import b64decode, b64encode
from datetime import UTC, datetime, timedelta
from hashlib import pbkdf2_hmac
from hmac import compare_digest
from secrets import token_bytes
from uuid import uuid4

from jose import JWTError, jwt

from .config import settings

PBKDF2_ITERATIONS = 100_000
SALT_BYTES = 16


def hash_password(password: str) -> str:
    salt = token_bytes(SALT_BYTES)
    derived = pbkdf2_hmac("sha256", password.encode("utf-8"), salt, PBKDF2_ITERATIONS)
    return f"{PBKDF2_ITERATIONS}${b64encode(salt).decode()}${b64encode(derived).decode()}"


def verify_password(password: str, password_hash: str) -> bool:
    try:
        iterations_str, salt_b64, digest_b64 = password_hash.split("$", maxsplit=2)
        iterations = int(iterations_str)
        salt = b64decode(salt_b64.encode())
        expected = b64decode(digest_b64.encode())
        candidate = pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
        return compare_digest(candidate, expected)
    except (TypeError, ValueError):
        return compare_digest(password, password_hash)


def create_access_token(user_id: str) -> str:
    now = datetime.now(UTC)
    payload = {
        "sub": user_id,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(seconds=settings.ACCESS_TOKEN_EXPIRE_SECONDS)).timestamp()),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


def decode_access_token(token: str) -> str | None:
    try:
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
    except JWTError:
        return None
    return payload.get("sub")


def create_refresh_token() -> str:
    return f"rft_{uuid4().hex}{uuid4().hex}"
