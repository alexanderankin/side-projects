"""run SQL file for category/example schema

Revision ID: 002_data_point_demo
Revises:
Create Date: 2026-04-05
"""

from pathlib import Path

from alembic import op

revision = "002_data_point_demo"
down_revision = "001_create_category_example"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        (Path(__file__).parent.parent / "sql" / f"{revision}_up.sql").read_text()
    )


def downgrade() -> None:
    op.execute(
        (Path(__file__).parent.parent / "sql" / f"{revision}_down.sql").read_text()
    )
