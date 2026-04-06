import uuid
from sqlalchemy.orm import Session
from sqlalchemy import select
from python_db_pg_crud_migrations.models import Category, Example


# ---- Category ----
def create_category(db: Session, name: str) -> Category:
    obj = Category(name=name)
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj


def get_category(db: Session, category_id: int) -> Category | None:
    return db.get(Category, category_id)


def list_categories(db: Session):
    return db.scalars(select(Category)).all()


def update_category(db: Session, category_id: int, name: str):
    obj = db.get(Category, category_id)
    if not obj:
        return None
    obj.name = name
    db.commit()
    db.refresh(obj)
    return obj


def delete_category(db: Session, category_id: int):
    obj = db.get(Category, category_id)
    if not obj:
        return False
    db.delete(obj)
    db.commit()
    return True


# ---- Example ----
def create_example(db: Session, category_id: int, name: str) -> Example:
    obj = Example(category_id=category_id, name=name)
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj


def get_example(db: Session, example_id: uuid.UUID) -> Example | None:
    return db.get(Example, example_id)


def list_examples_by_category(db: Session, category_id: int):
    return db.scalars(
        select(Example).where(Example.category_id == category_id)
    ).all()


def update_example(db: Session, example_id: uuid.UUID, name: str):
    obj = db.get(Example, example_id)
    if not obj:
        return None
    obj.name = name
    db.commit()
    db.refresh(obj)
    return obj


def delete_example(db: Session, example_id: uuid.UUID):
    obj = db.get(Example, example_id)
    if not obj:
        return False
    db.delete(obj)
    db.commit()
    return True
