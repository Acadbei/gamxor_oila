import json
import math
import secrets
from hashlib import sha256
from datetime import timedelta
from decimal import Decimal

from django.http import HttpRequest
from django.utils import timezone


def canonical_phone(phone: str) -> str:
    digits = "".join(character for character in str(phone or "") if character.isdigit())
    if len(digits) == 9:
        return f"998{digits}"
    if len(digits) >= 12 and digits.startswith("998"):
        return digits[-12:]
    return digits


def format_phone(phone: str) -> str:
    digits = canonical_phone(phone)
    if len(digits) == 12 and digits.startswith("998"):
        return f"+998 {digits[3:5]} {digits[5:8]} {digits[8:10]} {digits[10:12]}"
    return str(phone or "").strip()


def relative_time_label(value) -> str:
    if not value:
        return ""

    now = timezone.localtime(timezone.now())
    current = timezone.localtime(value)
    delta = now - current

    if delta < timedelta(minutes=1):
        return "Hozirgina"

    minutes = int(delta.total_seconds() // 60)
    if minutes < 60:
        return f"{minutes} daqiqa oldin"

    hours = minutes // 60
    if hours < 24:
        return f"{hours} soat oldin"

    days = hours // 24
    return f"{days} kun oldin"


def next_check_in_label(time_value) -> str:
    if not time_value:
        return ""
    return f"{time_value:%H:%M} da umumiy check-in"


def as_float(value) -> float:
    if isinstance(value, Decimal):
        return float(value)
    if value is None:
        return 0.0
    return float(value)


def parse_json_body(request: HttpRequest) -> dict:
    if not request.body:
        return {}

    try:
        payload = json.loads(request.body.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        raise ValueError("JSON body noto'g'ri.") from exc

    if not isinstance(payload, dict):
        raise ValueError("JSON body object bo'lishi kerak.")

    return payload


def pick(payload: dict, *keys, default=None):
    for key in keys:
        if key in payload:
            return payload[key]
    return default


def generate_token_key() -> str:
    return secrets.token_hex(24)


def token_digest(token: str) -> str:
    return sha256(str(token or "").encode("utf-8")).hexdigest()


def clamp_int(value, *, min_value: int, max_value: int, field_name: str) -> int:
    try:
        resolved = int(value)
    except (TypeError, ValueError) as exc:
        raise ValueError(f"{field_name} noto'g'ri.") from exc
    return max(min_value, min(max_value, resolved))


def parse_decimal(value, *, field_name: str) -> Decimal:
    try:
        resolved = Decimal(str(value))
    except Exception as exc:
        raise ValueError(f"{field_name} noto'g'ri.") from exc
    if not resolved.is_finite():
        raise ValueError(f"{field_name} noto'g'ri.")
    return resolved


def parse_lat_lon(value, *, field_name: str, min_value: str, max_value: str) -> Decimal:
    resolved = parse_decimal(value, field_name=field_name)
    if resolved < Decimal(min_value) or resolved > Decimal(max_value):
        raise ValueError(f"{field_name} diapazondan tashqarida.")
    return resolved


def clean_text(value, *, default: str = "", max_length: int | None = None) -> str:
    resolved = str(value if value is not None else default).strip()
    if max_length is not None:
        resolved = resolved[:max_length]
    return resolved
