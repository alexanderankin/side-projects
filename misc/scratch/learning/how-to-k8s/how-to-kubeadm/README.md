# how to set up a kubernetes cluster with kubeadm

## install tools

```shell
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.34/deb/Release.key --output /etc/apt/keyrings/k8s.asc
echo "deb [signed-by=/etc/apt/keyrings/k8s.asc] https://pkgs.k8s.io/core:/stable:/v1.34/deb/ /" | tee /etc/apt/sources.list.d/kubernetes.list
apt update
apt install kubeadm kubectl kubelet cri-tools kubernetes-cni

wget ''
```

## create single node cluster

```shell
kubeadm init \
  --apiserver-advertise-address 0.0.0.0 \
  --apiserver-bind-port 6443 \
  --apiserver-cert-extra-sans $(hostname) \
  --cert-dir /etc/kubernetes/pki \
  --control-plane-endpoint $(hostname) \
  
  
```
