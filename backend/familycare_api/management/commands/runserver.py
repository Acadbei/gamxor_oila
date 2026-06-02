from django.core.management import call_command
from django.core.management.commands.runserver import Command as DjangoRunserverCommand
from django.db.utils import OperationalError, ProgrammingError

from familycare_api.models import Family


class Command(DjangoRunserverCommand):
    help = "Run the development server and seed demo data automatically on an empty database."

    def handle(self, *args, **options):
        try:
            if not Family.objects.exists():
                self.stdout.write("Seeding demo data for local development...")
                call_command("seed_demo")
        except (OperationalError, ProgrammingError):
            # The database is not migrated yet. Let Django continue and surface the
            # normal error if the user skipped `migrate`.
            pass

        return super().handle(*args, **options)
