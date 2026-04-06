import uuid
from contextlib import asynccontextmanager
from logging import INFO, basicConfig, getLogger

from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy.orm import Session

from python_db_pg_crud_migrations.db import SessionLocal
from python_db_pg_crud_migrations import migrations
from python_db_pg_crud_migrations import crud
from python_db_pg_crud_migrations.models import NewCategory

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
def update_category(category_id: int, new_category: NewCategory, db: Session = Depends(get_db)):
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
