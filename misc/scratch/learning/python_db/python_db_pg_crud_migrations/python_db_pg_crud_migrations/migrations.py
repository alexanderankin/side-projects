from alembic import command
from alembic.config import Config
from pathlib import Path

from python_db_pg_crud_migrations import db


def run_migrations():
    alembic_cfg = Config()

    alembic_migrations = Path(__file__).parent / "alembic_migrations"
    children = [*alembic_migrations.iterdir()]
    if len(children) == 0:
        raise RuntimeError("no migrations")
    alembic_cfg.set_main_option("script_location", str(alembic_migrations))
    alembic_cfg.set_main_option("sqlalchemy.url", db.DATABASE_URL)

    command.upgrade(alembic_cfg, "head")
