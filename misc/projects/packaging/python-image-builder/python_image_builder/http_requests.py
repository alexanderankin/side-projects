from dataclasses import dataclass

from httpx import Auth


@dataclass
class BearerAuth(Auth):
    token: str

    def auth_flow(self, request):
        # Send the request, with a custom `X-Authentication` header.
        request.headers["Authorization"] = f"Bearer {self.token}"
        yield request
