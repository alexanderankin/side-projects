from os import getenv

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

DATABASE_URL = getenv(
    "DATABASE_URL", "postgresql+psycopg://postgres:postgres@localhost:5432/postgres"
)

engine = create_engine(DATABASE_URL, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
