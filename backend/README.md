# Family Care Backend

Django backend built for the mobile app in this repository.

## What it covers

- Phone-based OTP auth flow
- Caregiver profile and family setup
- Family members with live location fields
- Invitations for adding relatives, including recipient-side acceptance
- Notifications and activity feed
- SOS alerts and delivery tracking
- Admin broadcast messages to all families
- Demo seed data that matches the current Android mock data

## Quick start

```bash
cd backend
python -m pip install -r requirements.txt
python manage.py makemigrations
python manage.py migrate
python manage.py runserver
```

`python manage.py runserver` now seeds the demo data automatically the first time it starts against an empty database.

API base URL:

```text
http://127.0.0.1:8000/api/v1/
```

## Main endpoints

- `POST auth/request-code/`
- `POST auth/verify-code/`
- `POST auth/register/`
- `POST auth/login/`
- `POST auth/logout/`
- `GET dashboard/`
- `GET/PATCH me/profile/`
- `GET members/`
- `GET/PATCH members/<id>/`
- `POST members/<id>/location/`
- `GET/POST invitations/`
- `POST invitations/<id>/accept/`
- `GET notifications/`
- `POST notifications/read-all/`
- `POST notifications/<id>/read/`
- `DELETE notifications/<id>/`
- `GET activity-feed/`
- `GET sos/`
- `POST sos/trigger/`
- `POST sos/<id>/resolve/`

## Demo OTP

When `DJANGO_DEBUG=true`, the backend returns `otpHint` and accepts `2580`.

## Admin panel

Create a superuser and open `http://127.0.0.1:8000/admin/`.

From the admin panel you can:

- view profiles, family members, last seen time, addresses, SOS alerts, invitations, and notifications
- create a `BroadcastMessage` record to push one notification to every family at once
