# loki experiments

this is not unlike https://github.com/alexanderankin/prometheus-experiments

## Usage

* start loki-docker-compose
* start !!-test-flog-docker
* observe logs

optionally, run other components and point them to:

```alloy
loki.write "grafana_exporter_logs_write" {
  endpoint {
    // http://localhost:3100/loki/api/v1/push
    url = sys.env("GRAFANA_LOKI_WRITE_URL")
    basic_auth {
      username = sys.env("GRAFANA_CLOUD_USERNAME") // refer to compose
      password = sys.env("GRAFANA_CLOUD_PASSWORD")
    }
  }
}
```

some example configs (not all working) are in [`./configs`](./configs)

## Next steps

look at complete OTel example, with traces and metrics

## OTel compatibility

the OpenTelemetry website lists a variable for an endpoint
that can accept logs via POST request (or gRPC):

https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/

## notes

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

systemd fields - https://www.freedesktop.org/software/systemd/man/latest/systemd.journal-fields.html

## metrics

plan for metrics

https://grafana.com/docs/alloy/latest/reference/components/otelcol/otelcol.exporter.prometheus/#create-prometheus-labels-from-otlp-resource-attributes
