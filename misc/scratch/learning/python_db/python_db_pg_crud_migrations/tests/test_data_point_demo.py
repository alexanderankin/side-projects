import base64

import pytest
import requests

from python_db_pg_crud_migrations.data_point_demo import json_deserialize_data_point, json_serialize_data_point


@pytest.mark.parametrize("url_prefix", ["sqlalchemy", "manual"])
def test_bulk_insert_and_offset_pagination(api_base_url, faker, url_prefix):
    unique_x = faker.pyint()

    items = [
        {
            "x": unique_x,
            "y": i,
            "z": i * 2,
            "data_bytes": f"payload-{i}".encode()
            if url_prefix == "manual" else
            base64.b64encode(f"payload-{i}".encode()).decode(),
        }
        for i in range(50)
    ]

    if url_prefix == "manual":
        list(map(json_serialize_data_point, items))

    # bulk insert
    resp = requests.post(
        f"{api_base_url}/{url_prefix}/data-points/bulk",
        json=items,
    )
    assert resp.status_code == 200, resp.text

    # paginate 5 at a time
    seen = []
    for offset in range(0, 50, 5):
        resp = requests.get(
            f"{api_base_url}/{url_prefix}/data-points",
            params={
                "limit": 5,
                "offset": offset,
                "x": unique_x,  # future-proof filtering
            },
        )
        assert resp.status_code == 200, resp.text

        page = resp.json()
        assert len(page) <= 5

        for row in page:
            assert row["x"] == unique_x
            seen.append(row["id"])

    # ensure we got all 50 unique rows
    assert len(seen) == 50
    assert len(set(seen)) == 50


@pytest.mark.parametrize("url_prefix", ["sqlalchemy", "manual"])
def test_bulk_insert_and_keyset_pagination(api_base_url, faker, url_prefix):
    unique_x = faker.pyint()

    items = [
        {
            "x": unique_x,
            "y": i,
            "z": i * 3,
            "data_bytes": f"payload-{i}".encode()
            if url_prefix == "manual" else
            base64.b64encode(f"payload-{i}".encode()).decode(),
        }
        for i in range(100)
    ]
    if url_prefix == "manual":
        list(map(json_serialize_data_point, items))

    # bulk insert
    resp = requests.post(
        f"{api_base_url}/{url_prefix}/data-points/bulk",
        json=items
    )
    assert resp.status_code == 200, resp.text

    seen = []

    params = {"limit": 10, "x": unique_x}
    resp = requests.get(f"{api_base_url}/{url_prefix}/data-points", params=params)
    assert resp.status_code == 200, resp.text

    page = resp.json()
    assert page
    for row in page:
        assert row["x"] == unique_x
        seen.append(row["id"])
    last_id = page[-1]["id"]

    while True:
        params = {"limit": 10, "x": unique_x, "last_id": last_id}
        resp = requests.get(f"{api_base_url}/{url_prefix}/data-points", params=params)
        assert resp.status_code == 200, resp.text

        page = resp.json()
        if not page:
            break

        for row in page:
            assert row["x"] == unique_x
            seen.append(row["id"])

        last_id = page[-1]["id"]

    # ensure we got all 100 unique rows
    assert len(seen) == 100
    assert len(set(seen)) == 100

    # ensure ordering is strictly increasing (keyset guarantee)
    assert seen == sorted(seen)
