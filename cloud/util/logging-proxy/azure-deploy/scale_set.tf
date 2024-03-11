/*
locals {
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDM7MabEw9ex+CbFE5x7BkgwwC9BNERReO16rJEkiTPVkomzd/1cppLxPeaWMjW9ndsXmGXkr1dh0RVbqkof7Ns2lIr9EtKQ8IR5EF4qKRxcETApSP0sNvsrYXStqkJohKRxI9D8G+n9vODdQIcm9WQVNjFlc+Gg3tMtTShdbJQ8YFFXvta36jaKD3wVtkcfjyFD0+wop58zTaLk1qtlq3JbjqaYXaMKFdCkwHbg8QeMetJHlXCnB6gI8kR0NzaeB4uLuBloEZPgS1b/Em6yGYivSI3hyQQOKia0hW4pqPy3hf/TcEgYykK94VDMYuSErpvBdlShFLGWmVI3xFh2Tzu7AQaipOF0mBbe3aOqO1nm9noo0pUNok9QLSEInMXjNxUgjU0YTS/wh1uMVOiwAVTA+g1GY367gHON+ihdxw3obM/MXYlwbPNkFldeedyGNZs/JwlW2x1j17syKg5NFNtyjGU3pYic1C67AQxYrHt4SWLq1EI3BY256cVxUe/0T0= me"
}


resource "azurerm_virtual_network" "lp_vnet" {
  name                = "${local.prefix}-network"
  resource_group_name = azurerm_resource_group.lp_group.name
  location            = azurerm_resource_group.lp_group.location
  address_space       = ["10.0.0.0/27"] # [ '10.0.0.0', '10.0.0.31' ]
}

resource "azurerm_subnet" "lp_subnet" {
  name                 = "${local.prefix}-internal"
  resource_group_name  = azurerm_resource_group.lp_group.name
  virtual_network_name = azurerm_virtual_network.lp_vnet.name
  address_prefixes     = ["10.0.0.0/28"] # [ '10.0.0.0', '10.0.0.15' ]
}

*/
/*
resource "azurerm_public_ip" "lp_vm_bastion_public_ip" {
  name                = "${local.prefix}-bastion-pip"
  location            = azurerm_resource_group.lp_group.location
  resource_group_name = azurerm_resource_group.lp_group.name
  allocation_method   = "Static"
  sku                 = "Standard"
}
*/
/*

#
#resource "azurerm_bastion_host" "lp_vm_bastion" {
#  name                = "${local.prefix}-bh"
#  resource_group_name = azurerm_resource_group.lp_group.name
#  location            = azurerm_resource_group.lp_group.location
#  ip_configuration {
#    name                 = azurerm_public_ip.lp_public_ip.name
#    public_ip_address_id = azurerm_public_ip.lp_public_ip.id
#    subnet_id            = azurerm_subnet.lp_subnet.id
#  }
#}

resource "azurerm_linux_virtual_machine_scale_set" "lp_vmss" {
  name                = "${local.prefix}-vmss"
  resource_group_name = azurerm_resource_group.lp_group.name
  location            = azurerm_resource_group.lp_group.location
  sku                 = "Standard_B1s"
  instances           = 1
  admin_username      = "vminit"

  admin_ssh_key {
    username   = "vminit"
    public_key = local.public_key
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }

  os_disk {
    storage_account_type = "Standard_LRS"
    caching              = "ReadWrite"
    disk_size_gb         = 30
  }

  network_interface {
    name    = "example"
    primary = true

    ip_configuration {
      name      = "internal"
      primary   = true
      subnet_id = azurerm_subnet.lp_subnet.id
    }
  }
}
*/
