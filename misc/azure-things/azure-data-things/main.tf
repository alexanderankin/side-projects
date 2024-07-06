terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.111.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.53.1"
    }
  }
}

provider "azurerm" {
  features {}
  skip_provider_registration = true
  use_cli                    = true
  use_msi                    = false
  use_oidc                   = false
}
provider "azuread" {
  skip_provider_registration = true
  use_cli                    = true
  use_msi                    = false
  use_oidc                   = false
}

resource "azurerm_resource_group" "rg" {
  /*
  │ Error: "us-west" was not found in the list of supported Azure
  Locations: "australiacentral, australiacentral2, australiaeast,
  australiasoutheast, brazilsouth, brazilsoutheast, brazilus, canadacentral,
  canadaeast, centralindia, centralus, centraluseuap, eastasia, eastus,
  eastus2, eastus2euap, francecentral, francesouth, germanynorth,
  germanywestcentral, israelcentral, italynorth, japaneast, japanwest,
  jioindiacentral, jioindiawest, koreacentral, koreasouth, malaysiasouth,
  mexicocentral, newzealandnorth, northcentralus, northeurope, norwayeast,
  norwaywest, polandcentral, qatarcentral, southafricanorth, southafricawest,
  southcentralus, southeastasia, southindia, spaincentral, swedencentral,
  swedensouth, switzerlandnorth, switzerlandwest, uaecentral, uaenorth,
  uksouth, ukwest, westcentralus, westeurope, westindia, westus, westus2,
  westus3, australiaeastfoundational, austriaeast, chilecentral,
  eastusfoundational, eastusslv, indonesiacentral, israelnorthwest,
  malaysiawest, southcentralus2, southeastus, southwestus,
  uksouthfoundational"
   */
  location = "westus"
  name     = "azure-resource-group"
}

locals {
  storage_name   = "storage"
  storage_suffix = substr(sha256("hello"), 0, 24 - length(local.storage_name))
}

resource "azurerm_storage_account" "storage" {
  # Length between 3 and 24 characters with only lowercase letters and numbers.
  # https://learn.microsoft.com/en-us/azure/azure-resource-manager/troubleshooting/error-storage-account-name?tabs=bicep#account-name-invalid
  name                = "${local.storage_name}${local.storage_suffix}"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location

  account_replication_type = "LRS"      # LRS, GRS, RAGRS, ZRS, GZRS, RAGZRS
  account_tier             = "Standard" # Standard, Premium
}

resource "azurerm_storage_container" "container" {
  storage_account_name  = azurerm_storage_account.storage.name
  name                  = "container"
  container_access_type = "blob"
}

output "account_url" {
  value = azurerm_storage_account.storage.primary_blob_endpoint
}

output "account_access_key" {
  value     = azurerm_storage_account.storage.primary_access_key
  sensitive = true
}

output "account_container_name" {
  value = azurerm_storage_container.container.name
}
