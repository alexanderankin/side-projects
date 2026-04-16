import base64
from typing import Any, NotRequired, Sequence, TypedDict, cast

from sqlalchemy import (
    Column,
    Integer,
    LargeBinary,
    MetaData,
    Table,
    insert,
    select,
)
from sqlalchemy.orm import Session


class DataPointTD(TypedDict):
    id: NotRequired[int]
    x: int
    y: int
    z: int
    data_base64: NotRequired[str]
    data_bytes: NotRequired[bytes]


def json_deserialize_data_point(dp: DataPointTD) -> None:
    dp["data_bytes"] = base64.b64decode(dp["data_base64"])
    del dp["data_base64"]


def json_serialize_data_point(dp: DataPointTD) -> None:
    dp["data_base64"] = base64.b64encode(dp["data_bytes"]).decode()
    del dp["data_bytes"]


class DataPointQuery(TypedDict):
    x: NotRequired[int | None]
    y: NotRequired[int | None]
    z: NotRequired[int | None]


metadata = MetaData()

DataPointTable = Table(
    "data_point",
    metadata,
    Column("id", Integer, primary_key=True),
    Column("x", Integer, nullable=False),
    Column("y", Integer, nullable=False),
    Column("z", Integer, nullable=False),
    Column("data_bytes", LargeBinary, nullable=False),
)


def create_data_points(db: Session, data_points: list[DataPointTD]) -> Sequence[Any]:
    list(map(json_deserialize_data_point, data_points))
    result = db.execute(
        insert(DataPointTable).returning(DataPointTable.c.id), data_points
    )
    ids = result.scalars().all()
    db.commit()
    return ids


def get_one(db: Session, id_: int) -> DataPointTD | None:
    row = (
        db.execute(select(DataPointTable).where(DataPointTable.c.id == id_))
        .mappings()
        .first()
    )
    if row is None:
        return None
    return cast(DataPointTD, cast(object, dict(row)))


def get_all_offset(
    db: Session,
    limit: int,
    offset: int | None,
    query: DataPointQuery | None,
    range_min: DataPointQuery | None,
    range_max: DataPointQuery | None,
) -> Sequence[DataPointTD]:
    stmt = cast(
        Any,
        select(DataPointTable)
        .order_by(DataPointTable.c.id)
        .limit(limit)
        .offset(offset),
    )

    stmt = apply_filters(
        query=query, range_max=range_max, range_min=range_min, stmt=stmt
    )

    rows = db.execute(stmt).mappings().all()
    rows_dps = [cast(DataPointTD, cast(object, dict(row))) for row in rows]
    list(map(json_serialize_data_point, rows_dps))
    return rows_dps


# noinspection PyTypedDict
def apply_filters(
    query: DataPointQuery | None,
    range_max: DataPointQuery | None,
    range_min: DataPointQuery | None,
    stmt: Any,
) -> Any:
    axes = ("x", "y", "z")

    if query:
        for axis in axes:
            value = query.get(axis)
            if value is not None:
                stmt = stmt.where(getattr(DataPointTable.c, axis) == value)

    if range_min:
        for axis in axes:
            value = range_min.get(axis)
            if value is not None:
                stmt = stmt.where(getattr(DataPointTable.c, axis) >= value)

    if range_max:
        for axis in axes:
            value = range_max.get(axis)
            if value is not None:
                stmt = stmt.where(getattr(DataPointTable.c, axis) <= value)

    return stmt


def get_all_keyset(
    db: Session,
    limit: int,
    last_id: int | None,
    query: DataPointQuery | None,
    range_min: DataPointQuery | None,
    range_max: DataPointQuery | None,
) -> Sequence[DataPointTD]:
    stmt = cast(Any, select(DataPointTable).order_by(DataPointTable.c.id).limit(limit))
    stmt = apply_filters(
        query=query, range_max=range_max, range_min=range_min, stmt=stmt
    )

    if last_id is not None:
        stmt = stmt.where(DataPointTable.c.id > last_id)

    rows = db.execute(stmt).mappings().all()
    rows_dps = [cast(DataPointTD, cast(object, dict(row))) for row in rows]
    list(map(json_serialize_data_point, rows_dps))
    return rows_dps
