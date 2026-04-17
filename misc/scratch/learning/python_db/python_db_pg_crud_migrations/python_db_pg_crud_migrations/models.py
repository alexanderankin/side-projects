import base64
import uuid
from datetime import datetime

from pydantic import BaseModel, field_serializer, field_validator
from sqlalchemy import BigInteger, ForeignKey, String, UniqueConstraint, func
from sqlalchemy.dialects.postgresql import BYTEA, UUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship


class Base(DeclarativeBase):
    pass


class NewCategory(BaseModel):
    name: str


class Category(Base):
    __tablename__ = "category"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(150), nullable=False, unique=True)

    created_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False, onupdate=func.now()
    )

    examples: Mapped[list["Example"]] = relationship(
        back_populates="category", cascade="all, delete-orphan"
    )


class Example(Base):
    __tablename__ = "example"
    __table_args__ = (
        UniqueConstraint("category_id", "name", name="uq_example_category_name"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    category_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("category.id", ondelete="CASCADE"), nullable=False
    )
    name: Mapped[str] = mapped_column(String(150), nullable=False)

    created_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False, onupdate=func.now()
    )

    category: Mapped["Category"] = relationship(back_populates="examples")


class DataPoint(BaseModel):
    id: int | None = None
    x: int
    y: int
    z: int
    data_bytes: bytes

    @field_validator("data_bytes", mode="before")
    def decode(cls, v):
        if isinstance(v, str):
            return base64.b64decode(v)
        return v

    @field_serializer("data_bytes")
    def encode(self, v):
        return base64.b64encode(v).decode()


class DataPointEntity(Base):
    __tablename__ = "data_point"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    x: Mapped[int] = mapped_column(BigInteger, nullable=False)
    y: Mapped[int] = mapped_column(BigInteger, nullable=False)
    z: Mapped[int] = mapped_column(BigInteger, nullable=False)
    data_bytes: Mapped[bytes] = mapped_column(BYTEA, nullable=False)
