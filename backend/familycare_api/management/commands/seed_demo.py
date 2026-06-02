from datetime import timedelta
from decimal import Decimal

from django.contrib.auth.models import User
from django.core.management.base import BaseCommand
from django.db import transaction
from django.utils import timezone

from familycare_api.models import (
    ActivityFeedItem,
    AppNotification,
    AuthToken,
    CareProfile,
    Family,
    FamilyInvitation,
    FamilyMember,
    FeedSeverity,
    InvitationStatus,
    LocationPing,
    MemberStatus,
    NotificationCategory,
    OtpChallenge,
    SosAlert,
    SosAlertStatus,
    SosDelivery,
    TrustedPlace,
)
from familycare_api.services import create_activity, create_notification, ensure_family, sync_self_member


class Command(BaseCommand):
    help = "Seed Family Care backend with demo data that matches the mobile app."

    def add_arguments(self, parser):
        parser.add_argument("--reset", action="store_true", help="Delete existing demo records first.")

    @transaction.atomic
    def handle(self, *args, **options):
        if options["reset"]:
            SosDelivery.objects.all().delete()
            SosAlert.objects.all().delete()
            AppNotification.objects.all().delete()
            ActivityFeedItem.objects.all().delete()
            FamilyInvitation.objects.all().delete()
            FamilyMember.objects.all().delete()
            TrustedPlace.objects.all().delete()
            AuthToken.objects.all().delete()
            OtpChallenge.objects.all().delete()
            Family.objects.filter(owner__username__startswith="998").delete()
            CareProfile.objects.filter(user__username__startswith="998").delete()
            User.objects.filter(username__startswith="998").delete()

        owner_user, _ = User.objects.get_or_create(
            username="998903214567",
            defaults={"first_name": "Nodir"},
        )
        owner_profile, _ = CareProfile.objects.get_or_create(
            user=owner_user,
            defaults={
                "full_name": "Nodir Yusupov",
                "phone": "998903214567",
                "address": "Yunusobod, 9-kvartal",
                "emergency_contact": "998907770011",
                "bio": "Oilaviy monitoring, xavfsizlik va SOS boshqaruvi uchun mas'ulman.",
            },
        )
        owner_profile.full_name = "Nodir Yusupov"
        owner_profile.phone = "998903214567"
        owner_profile.address = "Yunusobod, 9-kvartal"
        owner_profile.emergency_contact = "998907770011"
        owner_profile.bio = "Oilaviy monitoring, xavfsizlik va SOS boshqaruvi uchun mas'ulman."
        owner_profile.save()

        family = ensure_family(owner_profile)
        family.name = "Yusupovlar oilasi"
        family.address = "Yunusobod, 9-kvartal"
        family.save()
        self_member = sync_self_member(owner_profile)
        self_member.latitude = Decimal("41.336715")
        self_member.longitude = Decimal("69.337875")
        self_member.address = "Buyuk Ipak Yo'li metro bekati"
        self_member.place_label = "Marshrut yakuni"
        self_member.battery_level = 86
        self_member.steps = 8420
        self_member.heart_rate = 79
        self_member.status = MemberStatus.MOVING
        self_member.note = "Toshkent bo'ylab bugungi marshrut yozib borilmoqda."
        self_member.safe_zone = "Uy, Amir Temur shoh ko'chasi va Buyuk Ipak Yo'li"
        self_member.schedule_label = "20:00 oilaviy check-in"
        self_member.distance_km = Decimal("8.40")
        self_member.save()

        member_rows = [
            {
                "name": "Anvar Yusupov",
                "relation": "Dada",
                "age": 61,
                "phone": "998901234567",
                "latitude": Decimal("41.311800"),
                "longitude": Decimal("69.281900"),
                "address": "Yunusobod, 4-daha",
                "place_label": "Uy atrofida",
                "battery_level": 81,
                "steps": 3821,
                "heart_rate": 74,
                "status": MemberStatus.SAFE,
                "note": "Tonggi yurishdan qaytdi.",
                "safe_zone": "Uy hududi",
                "schedule_label": "21:00 dori eslatmasi",
                "avatar_seed": 1,
                "distance_km": Decimal("2.80"),
            },
            {
                "name": "Dilnoza Yusupova",
                "relation": "Ona",
                "age": 56,
                "phone": "998935556677",
                "latitude": Decimal("41.299500"),
                "longitude": Decimal("69.240100"),
                "address": "Chilonzor, 12-kvartal",
                "place_label": "Ishdan qaytish yo'lida",
                "battery_level": 54,
                "steps": 6140,
                "heart_rate": 82,
                "status": MemberStatus.MOVING,
                "note": "Transportda harakatlanmoqda.",
                "safe_zone": "Ish va uy yo'nalishi",
                "schedule_label": "19:30 oilaviy qo'ng'iroq",
                "avatar_seed": 2,
                "distance_km": Decimal("4.90"),
            },
            {
                "name": "Bobur Yusupov",
                "relation": "O'g'il",
                "age": 14,
                "phone": "998977001122",
                "latitude": Decimal("41.327800"),
                "longitude": Decimal("69.281100"),
                "address": "Maktab, Olmazor tumani",
                "place_label": "Maktab hududi",
                "battery_level": 33,
                "steps": 7280,
                "heart_rate": 92,
                "status": MemberStatus.NEEDS_ATTENTION,
                "note": "Batareya past, zaryad kerak.",
                "safe_zone": "Maktab geozonasi",
                "schedule_label": "16:30 repititor",
                "avatar_seed": 3,
                "distance_km": Decimal("5.70"),
            },
            {
                "name": "O'ktam Yusupov",
                "relation": "Bobo",
                "age": 72,
                "phone": "998914449900",
                "latitude": Decimal("41.300000"),
                "longitude": Decimal("69.200000"),
                "address": "Sergeli, 6-bekat",
                "place_label": "Mahalla dorixonasi",
                "battery_level": 69,
                "steps": 2910,
                "heart_rate": 76,
                "status": MemberStatus.SAFE,
                "note": "Dorilarni olib uyga qaytmoqda.",
                "safe_zone": "Mahalla va dorixona",
                "schedule_label": "18:00 qand nazorati",
                "avatar_seed": 4,
                "distance_km": Decimal("1.90"),
            },
        ]

        bobur_member = None
        seeded_members = {self_member.phone: self_member}
        for defaults in member_rows:
            member, _ = FamilyMember.objects.update_or_create(
                family=family,
                phone=defaults["phone"],
                defaults=defaults,
            )
            seeded_members[member.phone] = member
            if member.phone == "998977001122":
                bobur_member = member

        LocationPing.objects.filter(member__family=family).delete()
        owner_route_rows = [
            ("998903214567", "41.310182", "69.279363", "Yunusobod, 9-kvartal", "Boshlanish: uy", 96, 1200, 76, "0.00"),
            ("998903214567", "41.310041", "69.279500", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 96, 1350, 76, "0.15"),
            ("998903214567", "41.309956", "69.279928", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 96, 1500, 76, "0.30"),
            ("998903214567", "41.309926", "69.280204", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 96, 1650, 76, "0.45"),
            ("998903214567", "41.309928", "69.280452", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 95, 1800, 76, "0.60"),
            ("998903214567", "41.309977", "69.280724", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 95, 1950, 76, "0.75"),
            ("998903214567", "41.310086", "69.280967", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 95, 2100, 76, "0.90"),
            ("998903214567", "41.310224", "69.281133", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 95, 2250, 76, "1.05"),
            ("998903214567", "41.310369", "69.281257", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 94, 2400, 76, "1.20"),
            ("998903214567", "41.310655", "69.281423", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 94, 2550, 76, "1.35"),
            ("998903214567", "41.311074", "69.281550", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 94, 2700, 76, "1.50"),
            ("998903214567", "41.311718", "69.281595", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 94, 2850, 76, "1.65"),
            ("998903214567", "41.311796", "69.281630", "Shahriston tomoni", "Ko'cha bo'ylab yurish", 93, 3000, 76, "1.80"),
            ("998903214567", "41.311865", "69.281701", "Shahriston tomoni", "Ko'cha bo'ylab yurish", 93, 3150, 76, "1.95"),
            ("998903214567", "41.312931", "69.284058", "Shahriston ko'chasi", "Ko'cha bo'ylab yurish", 93, 3300, 76, "2.10"),
            ("998903214567", "41.313883", "69.286185", "Shahriston ko'chasi", "Ko'cha bo'ylab yurish", 93, 3450, 76, "2.25"),
            ("998903214567", "41.315547", "69.289939", "Amir Temur shoh ko'chasi", "Ko'cha bo'ylab yurish", 92, 3600, 76, "2.40"),
            ("998903214567", "41.315910", "69.290765", "Amir Temur shoh ko'chasi", "Ko'cha bo'ylab yurish", 92, 3750, 76, "2.55"),
            ("998903214567", "41.317381", "69.294119", "Amir Temur shoh ko'chasi", "Ko'cha bo'ylab yurish", 92, 3900, 76, "2.70"),
            ("998903214567", "41.319621", "69.299261", "Amir Temur shoh ko'chasi", "Ko'cha bo'ylab yurish", 92, 4050, 76, "2.85"),
            ("998903214567", "41.320483", "69.301243", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 91, 4200, 76, "3.00"),
            ("998903214567", "41.320679", "69.301834", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 91, 4350, 76, "3.15"),
            ("998903214567", "41.320851", "69.302571", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 91, 4500, 76, "3.30"),
            ("998903214567", "41.320933", "69.303161", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 91, 4650, 76, "3.45"),
            ("998903214567", "41.320937", "69.303773", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 90, 4800, 76, "3.60"),
            ("998903214567", "41.320799", "69.305781", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 90, 4950, 76, "3.75"),
            ("998903214567", "41.320744", "69.307530", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 90, 5100, 76, "3.90"),
            ("998903214567", "41.320854", "69.308756", "Bodomzor yo'nalishi", "Ko'cha bo'ylab yurish", 90, 5250, 76, "4.05"),
            ("998903214567", "41.321023", "69.309667", "Minor tomoni", "Ko'cha bo'ylab yurish", 89, 5400, 76, "4.20"),
            ("998903214567", "41.321556", "69.311333", "Minor tomoni", "Ko'cha bo'ylab yurish", 89, 5550, 76, "4.35"),
            ("998903214567", "41.323441", "69.316762", "Minor tomoni", "Ko'cha bo'ylab yurish", 89, 5700, 76, "4.50"),
            ("998903214567", "41.323998", "69.318340", "Minor tomoni", "Ko'cha bo'ylab yurish", 89, 5850, 76, "4.65"),
            ("998903214567", "41.325106", "69.322221", "Kichik halqa yo'li", "Ko'cha bo'ylab yurish", 88, 6000, 76, "4.80"),
            ("998903214567", "41.325323", "69.323530", "Kichik halqa yo'li", "Ko'cha bo'ylab yurish", 88, 6150, 76, "4.95"),
            ("998903214567", "41.325462", "69.324506", "Kichik halqa yo'li", "Ko'cha bo'ylab yurish", 88, 6300, 76, "5.10"),
            ("998903214567", "41.325771", "69.325928", "Kichik halqa yo'li", "Ko'cha bo'ylab yurish", 88, 6450, 76, "5.25"),
            ("998903214567", "41.329616", "69.329988", "Pushkin ko'chasi", "Ko'cha bo'ylab yurish", 87, 6600, 76, "5.40"),
            ("998903214567", "41.332107", "69.332619", "Pushkin ko'chasi", "Ko'cha bo'ylab yurish", 87, 6750, 76, "5.55"),
            ("998903214567", "41.332923", "69.333482", "Pushkin ko'chasi", "Ko'cha bo'ylab yurish", 87, 6900, 76, "5.70"),
            ("998903214567", "41.334848", "69.335515", "Buyuk Ipak Yo'li tomoni", "Ko'cha bo'ylab yurish", 87, 7050, 76, "5.85"),
            ("998903214567", "41.337216", "69.338017", "Buyuk Ipak Yo'li metro bekati", "Ko'cha bo'ylab yurish", 86, 7200, 76, "6.00"),
            ("998903214567", "41.336715", "69.337875", "Buyuk Ipak Yo'li metro bekati", "Marshrut yakuni", 86, 7350, 76, "6.15"),
        ]
        history_rows = [
            *owner_route_rows,
            ("998901234567", "41.310900", "69.279400", "Yunusobod, 6-kvartal", "Ertalabki yurish", 84, 3010, 76, "2.10"),
            ("998901234567", "41.310910", "69.279910", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 84, 3180, 76, "2.20"),
            ("998901234567", "41.311030", "69.280360", "Yunusobod ichki ko'chasi", "Ko'cha bo'ylab yurish", 83, 3330, 75, "2.30"),
            ("998901234567", "41.311250", "69.280515", "Yunusobod 5-kvartal", "Ko'cha bo'ylab yurish", 83, 3460, 75, "2.40"),
            ("998901234567", "41.311420", "69.280742", "Yunusobod 5-kvartal", "Mahalla bog'i", 83, 3580, 75, "2.50"),
            ("998901234567", "41.311590", "69.281004", "Mahalla bog'i yonida", "Ko'cha bo'ylab yurish", 82, 3690, 74, "2.60"),
            ("998901234567", "41.311742", "69.281432", "Yunusobod 4-daha", "Ko'cha bo'ylab yurish", 82, 3770, 74, "2.70"),
            ("998901234567", "41.311800", "69.281900", "Yunusobod, 4-daha", "Uy atrofida", 81, 3821, 74, "2.80"),
            ("998935556677", "41.294800", "69.233400", "Chilonzor metro", "Yo'lga chiqdi", 61, 5200, 80, "4.10"),
            ("998935556677", "41.295050", "69.234060", "Chilonzor ko'chasi", "Ko'cha bo'ylab yurish", 61, 5350, 80, "4.20"),
            ("998935556677", "41.295780", "69.234258", "Chilonzor ko'chasi", "Ko'cha bo'ylab yurish", 60, 5480, 80, "4.30"),
            ("998935556677", "41.296233", "69.235000", "Chilonzor 9-kvartal", "Ko'cha bo'ylab yurish", 59, 5590, 81, "4.40"),
            ("998935556677", "41.297200", "69.237100", "Chilonzor ko'chasi", "Bekat yonida", 58, 5710, 81, "4.50"),
            ("998935556677", "41.297944", "69.237390", "Bekat orqa ko'chasi", "Ko'cha bo'ylab yurish", 57, 5860, 81, "4.60"),
            ("998935556677", "41.298640", "69.238420", "Chilonzor 12-kvartal", "Ko'cha bo'ylab yurish", 56, 6000, 82, "4.75"),
            ("998935556677", "41.299500", "69.240100", "Chilonzor, 12-kvartal", "Ishdan qaytish yo'lida", 54, 6140, 82, "4.90"),
            ("998977001122", "41.325500", "69.279000", "Olmazor tumani", "Maktabga kelish", 39, 6540, 89, "5.10"),
            ("998977001122", "41.325740", "69.279390", "Olmazor ichki ko'chasi", "Ko'cha bo'ylab yurish", 39, 6650, 89, "5.20"),
            ("998977001122", "41.326080", "69.279380", "Olmazor ichki ko'chasi", "Ko'cha bo'ylab yurish", 38, 6760, 90, "5.30"),
            ("998977001122", "41.326420", "69.279820", "Maktab yo'li", "Ko'cha bo'ylab yurish", 37, 6840, 90, "5.40"),
            ("998977001122", "41.326900", "69.280200", "Maktab kirishi", "Tanaffus payti", 36, 6905, 91, "5.50"),
            ("998977001122", "41.327160", "69.280580", "Maktab hovlisi atrofi", "Ko'cha bo'ylab yurish", 35, 7040, 91, "5.60"),
            ("998977001122", "41.327530", "69.280570", "Maktab orqa yo'li", "Ko'cha bo'ylab yurish", 34, 7160, 92, "5.65"),
            ("998977001122", "41.327800", "69.281100", "Maktab, Olmazor tumani", "Maktab hududi", 33, 7280, 92, "5.70"),
            ("998914449900", "41.298500", "69.197400", "Sergeli ichki ko'chasi", "Dorixonaga yo'l", 74, 2150, 75, "1.30"),
            ("998914449900", "41.298720", "69.197700", "Sergeli ichki ko'chasi", "Ko'cha bo'ylab yurish", 74, 2260, 75, "1.40"),
            ("998914449900", "41.298900", "69.198160", "Sergeli 6-mavze", "Ko'cha bo'ylab yurish", 73, 2380, 76, "1.50"),
            ("998914449900", "41.299200", "69.198800", "Mahalla markazi", "Dorixona oldi", 72, 2510, 76, "1.60"),
            ("998914449900", "41.299520", "69.198930", "Mahalla markazi yon yo'li", "Ko'cha bo'ylab yurish", 71, 2630, 76, "1.70"),
            ("998914449900", "41.299710", "69.199430", "Sergeli 6-bekat yo'li", "Ko'cha bo'ylab yurish", 70, 2770, 76, "1.80"),
            ("998914449900", "41.300000", "69.200000", "Sergeli, 6-bekat", "Mahalla dorixonasi", 69, 2910, 76, "1.90"),
        ]
        base_time = timezone.now() - timedelta(hours=2)
        seeded_pings = LocationPing.objects.bulk_create(
            [
                LocationPing(
                    member=seeded_members[phone],
                    latitude=Decimal(lat),
                    longitude=Decimal(lon),
                    address=address,
                    place_label=place_label,
                    battery_level=battery_level,
                    steps=steps,
                    heart_rate=heart_rate,
                    distance_km=Decimal(distance_km),
                )
                for phone, lat, lon, address, place_label, battery_level, steps, heart_rate, distance_km in history_rows
            ]
        )
        for index, ping in enumerate(seeded_pings):
            ping.created_at = base_time + timedelta(minutes=index * 8)
        LocationPing.objects.bulk_update(seeded_pings, ["created_at"])

        candidate_profiles = [
            {
                "username": "998942221100",
                "full_name": "Madina Yusupova",
                "phone": "998942221100",
                "address": "Mirzo Ulug'bek, TTZ-2",
                "avatar_seed": 5,
            },
            {
                "username": "998884107766",
                "full_name": "Jasur Karimov",
                "phone": "998884107766",
                "address": "Shayxontohur, Labzak",
                "avatar_seed": 6,
            },
        ]
        for candidate in candidate_profiles:
            user, _ = User.objects.get_or_create(username=candidate["username"])
            CareProfile.objects.update_or_create(
                user=user,
                defaults={key: value for key, value in candidate.items() if key != "username"},
            )

        if family.activity_items.count() < 3:
            family.activity_items.all().delete()
            create_activity(
                family,
                title="Ilova ishga tushdi",
                subtitle="Xarita va ro'yxatdan o'tish oqimi tayyor.",
                severity=FeedSeverity.POSITIVE,
            )
            create_activity(
                family,
                title="Dilnoza yo'lda",
                subtitle="Chilonzordan Yunusobod tomonga harakatlanyapti.",
                severity=FeedSeverity.NEUTRAL,
                member=FamilyMember.objects.get(family=family, phone="998935556677"),
            )
            if bobur_member:
                create_activity(
                    family,
                    title="Bobur SOS yubordi",
                    subtitle="Maktab hududidan tezkor signal kelgan.",
                    severity=FeedSeverity.WARNING,
                    member=bobur_member,
                )

        if family.notifications.count() < 4:
            family.notifications.all().delete()
            create_notification(
                family,
                title="Profil faol",
                message="Sizning profilingiz ro'yxatdan o'tgan va saqlab qo'yilgan.",
                category=NotificationCategory.SYSTEM,
            )
            create_notification(
                family,
                title="Boburdan SOS signali",
                message="Maktab hududidan tezkor yordam signali keldi.",
                category=NotificationCategory.SAFETY,
            )
            create_notification(
                family,
                title="Yunusobodda kechki ogohlantirish",
                message="Mahalladagi ichki ko'chada shubhali shaxs haqida xabar kelib tushdi.",
                category=NotificationCategory.CRIME,
            )
            create_notification(
                family,
                title="Olmazorda telefon o'g'irlash holati",
                message="Maktab yaqinida telefon tortib olish bo'yicha murojaat qayd etildi.",
                category=NotificationCategory.CRIME,
            )

        if bobur_member and not family.sos_alerts.filter(status=SosAlertStatus.ACTIVE).exists():
            SosAlert.objects.create(
                family=family,
                triggered_by=bobur_member,
                summary="Maktab hududidan tezkor yordam signali keldi.",
                address=bobur_member.address,
                status=SosAlertStatus.ACTIVE,
            )

        if not family.invitations.exists():
            FamilyInvitation.objects.create(
                family=family,
                invited_by=owner_profile,
                platform_profile=CareProfile.objects.filter(phone="998942221100").first(),
                name="Madina Yusupova",
                relation="Singil",
                phone="998942221100",
                status=InvitationStatus.PENDING_ACCEPTANCE,
                is_platform_user=True,
            )

        self.stdout.write(self.style.SUCCESS("Demo data seeded successfully."))
