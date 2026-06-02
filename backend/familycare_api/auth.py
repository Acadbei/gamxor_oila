from functools import wraps

from django.http import JsonResponse
from django.utils import timezone

from .models import AuthToken
from .utils import token_digest


TOKEN_TOUCH_INTERVAL_SECONDS = 300
SUPPORTED_AUTH_SCHEMES = {"token", "bearer"}


def extract_token_key(header_value: str) -> str | None:
    header = str(header_value or "").strip()
    if not header:
        return None

    scheme, separator, credentials = header.partition(" ")
    if not separator:
        return header

    if scheme.lower() not in SUPPORTED_AUTH_SCHEMES:
        return None

    token_key = credentials.strip()
    return token_key or None


def get_token_from_request(request):
    header = (
        request.META.get("HTTP_AUTHORIZATION")
        or request.META.get("HTTP_X_AUTHORIZATION")
        or request.META.get("REDIRECT_HTTP_AUTHORIZATION")
        or ""
    )
    token_key = extract_token_key(header)
    if not token_key:
        return None

    hashed_key = token_digest(token_key)
    token = (
        AuthToken.objects.select_related("user", "user__care_profile", "user__care_profile__family")
        .filter(key__in=[hashed_key, token_key])
        .first()
    )
    if not token:
        return None
    if token.expires_at <= timezone.now():
        token.delete()
        return None

    now = timezone.now()
    if (
        token.last_used_at is None
        or (now - token.last_used_at).total_seconds() >= TOKEN_TOUCH_INTERVAL_SECONDS
    ):
        token.last_used_at = now
        token.save(update_fields=["last_used_at"])
    return token


def api_login_required(view_func):
    @wraps(view_func)
    def wrapped(request, *args, **kwargs):
        token = get_token_from_request(request)
        if not token:
            return JsonResponse({"detail": "Autentifikatsiya kerak."}, status=401)
        if not hasattr(token.user, "care_profile"):
            return JsonResponse({"detail": "Profil topilmadi."}, status=403)

        request.auth_token = token
        request.profile = token.user.care_profile
        return view_func(request, *args, **kwargs)

    return wrapped
