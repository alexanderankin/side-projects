terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.89.0"
    }
  }
}

provider "azurerm" {
  features {
  }
  use_cli  = true
  use_msi  = false
  use_oidc = false
}

locals {
  prefix = "logging-proxy"
}

// https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli

# Create a resource group
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-resource-group
resource "azurerm_resource_group" "example" {
  name     = "${local.prefix}-deployment"
  location = "eastus2"
}

# Create a virtual network
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-virtual-network
/*
  az network vnet create \
    --resource-group logging-proxy-deployment \
    --location eastus2 \
    --name logging-proxy-vnet \
    --address-prefixes 10.1.0.0/16 \
    --subnet-name myBackendSubnet \
    --subnet-prefixes 10.1.0.0/24

terraform import azurerm_virtual_network.example
/subscriptions/$sub/resourceGroups/logging-proxy-deployment/providers/Microsoft.Network/virtualNetworks/logging-proxy-vnet
*/
resource "azurerm_virtual_network" "example" {
  address_space       = ["10.1.0.0/16"] # [ '10.1.0.0', '10.1.255.255' ]
  name                = "${local.prefix}-vnet"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location
}

/*
terraform import azurerm_subnet.example
/subscriptions/$sub/resourceGroups/logging-proxy-deployment/providers/Microsoft.Network/virtualNetworks/logging-proxy-vnet/subnets/myBackendSubnet
 */
resource "azurerm_subnet" "example" {
  address_prefixes     = ["10.1.0.0/24"] # [ '10.1.0.0', '10.1.0.255' ]
  name                 = "myBackendSubnet"
  resource_group_name  = azurerm_resource_group.example.name
  virtual_network_name = azurerm_virtual_network.example.name
}

# later make it very permissive
resource "azurerm_subnet_network_security_group_association" "aha" {
  network_security_group_id = azurerm_network_security_group.very_permissive.id
  subnet_id                 = azurerm_subnet.example.id
}

# Create a public IP address
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-public-ip-address
/*
  az network public-ip create \
    --resource-group logging-proxy-deployment \
    --name myPublicIP \
    --sku Standard \
    --zone 1 2 3
terraform import azurerm_public_ip.example
/subscriptions/$sub/resourceGroups/logging-proxy-deployment/providers/Microsoft.Network/publicIPAddresses/myPublicIP
*/
resource "azurerm_public_ip" "example" {
  allocation_method       = "Static"
  name                    = "myPublicIP"
  resource_group_name     = azurerm_resource_group.example.name
  location                = azurerm_resource_group.example.location
  sku                     = "Standard"
  zones                   = [1, 2, 3]
  idle_timeout_in_minutes = 30
}

# Create a load balancer
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-load-balancer
/*
  az network lb create \
    --name myLoadBalancer \
    --resource-group CreatePubLBQS-rg \
    --sku Standard \
    --public-ip-address myPublicIP \
    --frontend-ip-name myFrontEnd \
    --backend-pool-name myBackEndPool
terraform import azurerm_lb.example
/subscriptions/$sub/resourceGroups/logging-proxy-deployment/providers/Microsoft.Network/loadBalancers/lb1
*/
resource "azurerm_lb" "example" {
  name                = "lb1"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location
  sku                 = "Standard"
  frontend_ip_configuration {
    name                 = "myFrontEnd"
    public_ip_address_id = azurerm_public_ip.example.id
  }
}

resource "azurerm_lb_backend_address_pool" "example" {
  loadbalancer_id = azurerm_lb.example.id
  name            = "myBackEndPool"
}

# Create the health probe
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-the-health-probe
/*
  az network lb probe create \
    --resource-group CreatePubLBQS-rg \
    --lb-name myLoadBalancer \
    --name myHealthProbe \
    --protocol tcp \
    --port 80
*/
resource "azurerm_lb_probe" "example" {
  loadbalancer_id = azurerm_lb.example.id
  name            = "myHealthProbe"
  protocol        = "Tcp"
  port            = 80
}

# Create the load balancer rule
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-the-load-balancer-rule
/*
  az network lb rule create \
    --resource-group CreatePubLBQS-rg \
    --lb-name myLoadBalancer \
    --name myHTTPRule \
    --protocol tcp \
    --frontend-port 80 \
    --backend-port 80 \
    --frontend-ip-name myFrontEnd \
    --backend-pool-name myBackEndPool \
    --probe-name myHealthProbe \
    --disable-outbound-snat true \
    --idle-timeout 15 \
    --enable-tcp-reset true
*/
resource "azurerm_lb_rule" "example" {
  backend_port                   = 8000
  frontend_ip_configuration_name = "myFrontEnd"
  frontend_port                  = 80
  loadbalancer_id                = azurerm_lb.example.id
  name                           = "myHTTPRule"
  protocol                       = "Tcp"
  backend_address_pool_ids       = [azurerm_lb_backend_address_pool.example.id]
}

# Create a network security group
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-network-security-group
/*
  az network nsg create \
    --resource-group CreatePubLBQS-rg \
    --name myNSG
*/
resource "azurerm_network_security_group" "example" {
  name                = "myNSG"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location
}

# Create a network security group rule
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-network-security-group-rule
/*
  az network nsg rule create \
    --resource-group CreatePubLBQS-rg \
    --nsg-name myNSG \
    --name myNSGRuleHTTP \
    --protocol '*' \
    --direction inbound \
    --source-address-prefix '*' \
    --source-port-range '*' \
    --destination-address-prefix '*' \
    --destination-port-range 80 \
    --access allow \
    --priority 200
*/
resource "azurerm_network_security_rule" "example" {
  access                      = "Allow"
  direction                   = "Inbound"
  name                        = "myNSGRuleHTTP"
  network_security_group_name = azurerm_network_security_group.example.name
  priority                    = 200
  protocol                    = "Tcp"
  resource_group_name         = azurerm_resource_group.example.name
  source_port_range           = "*"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  destination_port_range      = "80"
}

resource "azurerm_network_security_group" "very_permissive" {
  name                = "veryPermissive"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location
}

resource "azurerm_network_security_rule" "very_permissive_rule" {
  for_each = toset(["Inbound", "Outbound"])

  access                      = "Allow"
  direction                   = each.key
  name                        = "veryPermissive-${each.key}"
  network_security_group_name = azurerm_network_security_group.very_permissive.name
  priority                    = each.key == "Inbound" ? 200 : 201
  protocol                    = "Tcp"
  resource_group_name         = azurerm_resource_group.example.name
  source_port_range           = "*"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  destination_port_range      = "*"
}

## Create a bastion host
## https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-bastion-host
## Create a public IP address
## https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-public-ip-address-1
#/*
#  az network public-ip create \
#    --resource-group CreatePubLBQS-rg \
#    --name myBastionIP \
#    --sku Standard \
#    --zone 1 2 3
#*/
#resource "azurerm_public_ip" "bastion" {
#  allocation_method       = "Static"
#  name                    = "myBastionIP"
#  resource_group_name     = azurerm_resource_group.example.name
#  location                = azurerm_resource_group.example.location
#  sku                     = "Standard"
#  zones                   = [1, 2, 3]
#  idle_timeout_in_minutes = 30
#}
#
## Create a bastion subnet
## https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-a-bastion-subnet
#/*
#  az network vnet subnet create \
#    --resource-group CreatePubLBQS-rg \
#    --name AzureBastionSubnet \
#    --vnet-name myVNet \
#    --address-prefixes 10.1.1.0/27
#*/
#resource "azurerm_subnet" "AzureBastionSubnet" {
#  address_prefixes     = ["10.1.1.0/27"] # [ '10.1.1.0', '10.1.1.31' ]
#  name                 = "AzureBastionSubnet"
#  resource_group_name  = azurerm_resource_group.example.name
#  virtual_network_name = azurerm_virtual_network.example.name
#}
#
## Create bastion host
## https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-bastion-host
#resource "azurerm_bastion_host" "example" {
#  name                = "example"
#  resource_group_name = azurerm_resource_group.example.name
#  location            = azurerm_resource_group.example.location
#  ip_configuration {
#    name                 = "configuration"
#    public_ip_address_id = azurerm_public_ip.bastion.id
#    subnet_id            = azurerm_subnet.AzureBastionSubnet.id
#  }
#}

// and when that fails, just create a bastion:

resource "azurerm_public_ip" "jump_host" {
  allocation_method       = "Static"
  name                    = "myJumpHostIP"
  resource_group_name     = azurerm_resource_group.example.name
  location                = azurerm_resource_group.example.location
  sku                     = "Standard"
  zones                   = [1, 2, 3]
  idle_timeout_in_minutes = 30
}

resource "azurerm_network_interface" "jump_host" {
  name                = "jump-host-internal"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location
  ip_configuration {
    name                          = "internal"
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.example.id
    primary                       = true
    #network_security_group_id = azurerm_network_security_group.very_permissive.id
  }
  ip_configuration {
    name                          = "external"
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.example.id
    public_ip_address_id          = azurerm_public_ip.jump_host.id
    #network_security_group_id = azurerm_network_security_group.very_permissive.id
  }
}

resource "azurerm_linux_virtual_machine" "jump_host" {
  name                = "jump-host"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location

  admin_username                  = "vminit"
  #admin_password = "vminitA1"
  disable_password_authentication = true

  size = "Standard_B1s"

  network_interface_ids = [azurerm_network_interface.jump_host.id]

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }
  admin_ssh_key {
    username   = "vminit"
    public_key = file("~/.ssh/id_rsa.pub")
  }
  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }
}


# Create backend servers
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-backend-servers
/*
  array=(myNicVM1 myNicVM2)
  for vmnic in "${array[@]}"
  do
    az network nic create \
        --resource-group CreatePubLBQS-rg \
        --name $vmnic \
        --vnet-name myVNet \
        --subnet myBackEndSubnet \
        --network-security-group myNSG
  done
*/

locals {
  machines = {
    "m1" = {
      "nic" : "n1"
    }
    "m2" = {
      "nic" : "n2"
    }
  }
}

# Create network interfaces for the virtual machines
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-network-interfaces-for-the-virtual-machines
resource "azurerm_network_interface" "nic" {
  for_each = local.machines

  name                = "nic-${each.value.nic}"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location

  ip_configuration {
    name                          = "internal"
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.example.id
  }
}

# Create virtual machines
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#create-virtual-machines
/*
  az vm create \
    --resource-group CreatePubLBQS-rg \
    --name myVM1 \
    --nics myNicVM1 \
    --image win2019datacenter \
    --admin-username azureuser \
    --zone 1 \
    --no-wait
  az vm create \
    --resource-group CreatePubLBQS-rg \
    --name myVM2 \
    --nics myNicVM2 \
    --image win2019datacenter \
    --admin-username azureuser \
    --zone 2 \
    --no-wait
*/
resource "azurerm_linux_virtual_machine" "vm1" {
  for_each = local.machines

  name                = "vm-${each.key}"
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location

  admin_username        = "vminit"
  admin_password        = "vminitA1!"
  network_interface_ids = [azurerm_network_interface.nic[each.key].id]
  size                  = "Standard_B1s"

  admin_ssh_key {
    username   = "vminit"
    public_key = file("~/.ssh/id_rsa.pub")
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }

  user_data = filebase64("${path.module}/user_data.sh")
}

# Add virtual machines to load balancer backend pool
# https://learn.microsoft.com/en-us/azure/load-balancer/quickstart-load-balancer-standard-public-cli#add-virtual-machines-to-load-balancer-backend-pool
/*
  array=(myNicVM1 myNicVM2)
  for vmnic in "${array[@]}"
  do
    az network nic ip-config address-pool add \
     --address-pool myBackendPool \
     --ip-config-name ipconfig1 \
     --nic-name $vmnic \
     --resource-group CreatePubLBQS-rg \
     --lb-name myLoadBalancer
  done
*/
resource "azurerm_network_interface_backend_address_pool_association" "example" {
  for_each = local.machines

  backend_address_pool_id = azurerm_lb_backend_address_pool.example.id

  network_interface_id  = azurerm_network_interface.nic[each.key].id
  ip_configuration_name = "internal"
}
