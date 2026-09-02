sudo ip tuntap add dev tap0 mode tap user "$USER"
sudo ip addr add 10.10.10.1/24 dev tap0
sudo ip link set tap0 up
