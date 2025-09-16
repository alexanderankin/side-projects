this is not unlike https://github.com/alexanderankin/prometheus-experiments

current attempt:

https://grafana.com/docs/loki/latest/get-started/quick-start/quick-start/

https://github.com/grafana/loki/tree/2f41584c4cea90083182d957ad97465113ee3b77/examples

get machine ip:

```shell
echo $(ip a show $(ip route show | grep ^default | sed -n 1p | grep -Eo 'dev \w+' | awk ' { print $NF } ' | head -n1 || :;) | grep 'inet ' | awk ' { print $2 } ' | cut -d'/' -f1);
```

running locally:

wget https://github.com/grafana/alloy/releases/download/v1.10.2/alloy-linux-amd64.zip

./alloy-linux-amd64 run --server.http.listen-addr=0.0.0.0:12345 systemd-local.alloy
