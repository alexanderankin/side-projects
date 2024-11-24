from typing import *

from pydantic import BaseModel, Field


class TLSInfo(BaseModel):
    """
        None model
            Information about the issuer of leaf TLS certificates and the trusted root
    CA certificate.


    """

    TrustRoot: Optional[str] = Field(alias="TrustRoot", default=None)

    CertIssuerSubject: Optional[str] = Field(alias="CertIssuerSubject", default=None)

    CertIssuerPublicKey: Optional[str] = Field(alias="CertIssuerPublicKey", default=None)
