from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("familycare_api", "0002_broadcastmessage"),
    ]

    operations = [
        migrations.AlterField(
            model_name="careprofile",
            name="avatar_uri",
            field=models.TextField(blank=True),
        ),
        migrations.AlterField(
            model_name="familymember",
            name="avatar_uri",
            field=models.TextField(blank=True),
        ),
    ]
