import requests


def _create_category(api_base_url: str, name: str) -> dict:
    response = requests.post(
        f"{api_base_url}/categories",
        json={"name": name},
        timeout=5,
    )
    assert response.status_code == 200, response.text
    return response.json()


def test_create_category(api_base_url, faker):
    name = faker.unique.word() + "_cat"
    response = requests.post(
        f"{api_base_url}/categories",
        json={"name": name},
        timeout=5,
    )

    assert response.status_code == 200, response.text
    data = response.json()
    assert data["id"] > 0
    assert data["name"] == name
    assert "created_at" in data
    assert "updated_at" in data


def test_list_categories(api_base_url, faker):
    created = _create_category(api_base_url, faker.unique.word() + "_list")

    response = requests.get(f"{api_base_url}/categories", timeout=5)

    assert response.status_code == 200, response.text
    data = response.json()
    assert isinstance(data, list)
    assert any(item["id"] == created["id"] and item["name"] == created["name"] for item in data)


def test_get_category(api_base_url, faker):
    created = _create_category(api_base_url, faker.unique.word() + "_get")

    response = requests.get(f"{api_base_url}/categories/{created['id']}", timeout=5)

    assert response.status_code == 200, response.text
    data = response.json()
    assert data["id"] == created["id"]
    assert data["name"] == created["name"]


def test_update_category(api_base_url, faker):
    created = _create_category(api_base_url, faker.unique.word() + "_before")
    new_name = faker.unique.word() + "_after"

    response = requests.put(
        f"{api_base_url}/categories/{created['id']}",
        json={"name": new_name},
        timeout=5,
    )

    assert response.status_code == 200, response.text
    data = response.json()
    assert data["id"] == created["id"]
    assert data["name"] == new_name

    verify = requests.get(f"{api_base_url}/categories/{created['id']}", timeout=5)
    assert verify.status_code == 200, verify.text
    verify_data = verify.json()
    assert verify_data["name"] == new_name


def test_delete_category(api_base_url, faker):
    created = _create_category(api_base_url, faker.unique.word() + "_delete")

    response = requests.delete(
        f"{api_base_url}/categories/{created['id']}",
        timeout=5,
    )

    assert response.status_code == 200, response.text
    assert response.json() == {"ok": True}

    verify = requests.get(f"{api_base_url}/categories/{created['id']}", timeout=5)
    assert verify.status_code == 404, verify.text
