import uuid
from contextlib import asynccontextmanager
from logging import INFO, basicConfig, getLogger

from fastapi import Depends, FastAPI, HTTPException, Query
from sqlalchemy.orm import Session

from python_db_pg_crud_migrations import crud
from python_db_pg_crud_migrations import data_point_demo, migrations
from python_db_pg_crud_migrations.data_point_demo import DataPointQuery, DataPointTD
from python_db_pg_crud_migrations.db import SessionLocal
from python_db_pg_crud_migrations.models import DataPoint, NewCategory

basicConfig(level=INFO)
logger = getLogger(__name__)
logger.info("created logger")


# noinspection PyUnusedLocal
@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("starting lifespan")
    logger.info("running migrations")
    migrations.run_migrations()
    logger.info("ran migrations")
    yield


app = FastAPI(lifespan=lifespan)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# ---- Category endpoints ----
@app.post("/categories")
def create_category(new_category: NewCategory, db: Session = Depends(get_db)):
    return crud.create_category(db, new_category.name)


@app.get("/categories")
def list_categories(db: Session = Depends(get_db)):
    return crud.list_categories(db)


@app.get("/categories/{category_id}")
def get_category(category_id: int, db: Session = Depends(get_db)):
    obj = crud.get_category(db, category_id)
    if not obj:
        raise HTTPException(404)
    return obj


@app.put("/categories/{category_id}")
def update_category(
    category_id: int, new_category: NewCategory, db: Session = Depends(get_db)
):
    obj = crud.update_category(db, category_id, new_category.name)
    if not obj:
        raise HTTPException(404)
    return obj


@app.delete("/categories/{category_id}")
def delete_category(category_id: int, db: Session = Depends(get_db)):
    if not crud.delete_category(db, category_id):
        raise HTTPException(404)
    return {"ok": True}


# ---- Example endpoints ----
@app.post("/examples")
def create_example(category_id: int, name: str, db: Session = Depends(get_db)):
    return crud.create_example(db, category_id, name)


@app.get("/examples/{example_id}")
def get_example(example_id: uuid.UUID, db: Session = Depends(get_db)):
    obj = crud.get_example(db, example_id)
    if not obj:
        raise HTTPException(404)
    return obj


@app.get("/categories/{category_id}/examples")
def list_examples(category_id: int, db: Session = Depends(get_db)):
    return crud.list_examples_by_category(db, category_id)


@app.put("/examples/{example_id}")
def update_example(example_id: uuid.UUID, name: str, db: Session = Depends(get_db)):
    obj = crud.update_example(db, example_id, name)
    if not obj:
        raise HTTPException(404)
    return obj


@app.delete("/examples/{example_id}")
def delete_example(example_id: uuid.UUID, db: Session = Depends(get_db)):
    if not crud.delete_example(db, example_id):
        raise HTTPException(404)
    return {"ok": True}


@app.post("/sqlalchemy/data-points/bulk")
def sqla_create_data_points_bulk(
    data_points: list[DataPoint], db: Session = Depends(get_db)
):
    return crud.data_point_create_bulk(db, data_points)


@app.get("/sqlalchemy/data-points")
def sqla_get_data_points(
    db: Session = Depends(get_db),
    limit: int = Query(100, ge=1, le=1000),
    offset: int | None = Query(None, ge=0),
    last_id: int | None = Query(None, ge=1),
    x: int | None = Query(None),
    y: int | None = Query(None),
    z: int | None = Query(None),
    x_min: int | None = Query(None),
    y_min: int | None = Query(None),
    z_min: int | None = Query(None),
    x_max: int | None = Query(None),
    y_max: int | None = Query(None),
    z_max: int | None = Query(None),
):
    query = DataPointQuery(x=x, y=y, z=z) if any((x, y, z)) else None
    q_min = (
        DataPointQuery(x=x_min, y=y_min, z=z_min)
        if any((x_min, y_min, z_min))
        else None
    )
    q_max = (
        DataPointQuery(x=x_max, y=y_max, z=z_max)
        if any((x_max, y_max, z_max))
        else None
    )
    return crud.data_point_get_all(db, limit, offset, last_id, query, q_min, q_max)


@app.post("/manual/data-points/bulk")
def create_data_points_bulk(
    data_points: list[DataPointTD], db: Session = Depends(get_db)
):
    return data_point_demo.create_data_points(db, data_points)


@app.get("/manual/data-points")
def get_data_points(
    db: Session = Depends(get_db),
    limit: int = Query(100, ge=1, le=1000),
    offset: int | None = Query(None, ge=0),
    last_id: int | None = Query(None, ge=1),
    x: int | None = Query(None),
    y: int | None = Query(None),
    z: int | None = Query(None),
    x_min: int | None = Query(None),
    y_min: int | None = Query(None),
    z_min: int | None = Query(None),
    x_max: int | None = Query(None),
    y_max: int | None = Query(None),
    z_max: int | None = Query(None),
):
    if offset is not None and last_id is not None:
        raise HTTPException(
            status_code=400, detail="offset/last_id are mutually exclusive"
        )

    query = DataPointQuery(x=x, y=y, z=z) if any((x, y, z)) else None
    q_min = (
        DataPointQuery(x=x_min, y=y_min, z=z_min)
        if any((x_min, y_min, z_min))
        else None
    )
    q_max = (
        DataPointQuery(x=x_max, y=y_max, z=z_max)
        if any((x_max, y_max, z_max))
        else None
    )

    if last_id is not None:
        return data_point_demo.get_all_keyset(db, limit, last_id, query, q_min, q_max)
    else:
        return data_point_demo.get_all_offset(
            db, limit, offset or 0, query, q_min, q_max
        )
