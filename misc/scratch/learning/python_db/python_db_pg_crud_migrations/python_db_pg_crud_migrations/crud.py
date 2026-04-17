import uuid
from typing import Any, Sequence, cast

from sqlalchemy.orm import Mapper, Session
from sqlalchemy import select

from python_db_pg_crud_migrations.data_point_demo import DataPointQuery
from python_db_pg_crud_migrations.models import (
    Category,
    DataPoint,
    DataPointEntity,
    Example,
)


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
    return db.scalars(select(Example).where(Example.category_id == category_id)).all()


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


def data_point_create_bulk(db: Session, data_points: list[DataPoint]):
    d = [e.__dict__ for e in data_points]
    data_point_entity_mapper = cast(Mapper[Any], cast(object, DataPointEntity))
    db.bulk_insert_mappings(data_point_entity_mapper, d)
    db.commit()


def data_point_get_all(
    db: Session,
    limit: int,
    offset: int | None,
    last_id: int | None,
    query: DataPointQuery | None,
    range_min: DataPointQuery | None,
    range_max: DataPointQuery | None,
) -> Sequence[DataPointEntity]:
    statement = select(DataPointEntity).limit(limit)

    if offset is not None:
        statement = statement.offset(offset)
    elif last_id is not None:
        statement = statement.where(DataPointEntity.id > last_id)

    fields = ("x", "y", "z")
    if query is not None:
        for each_field in fields:
            if (q_val := query.get(each_field)) is not None:
                statement = statement.where(DataPointEntity.x == q_val)
    if range_min is not None:
        for each_field in fields:
            if (r_min_val := range_min.get(each_field)) is not None:
                statement = statement.where(DataPointEntity.x >= r_min_val)
    if range_max is not None:
        for each_field in fields:
            if (r_max_val := range_max.get(each_field)) is not None:
                statement = statement.where(DataPointEntity.x < r_max_val)
    return db.execute(statement).scalars().all()
