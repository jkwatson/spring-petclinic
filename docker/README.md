# OpenTelemetry Collector Demo

*IMPORTANT:* This uses a pre-released version of the OpenTelemetry Collector.

This demo uses `docker-compose` and by default runs against the
`otel/opentelemetry-collector-dev:latest` image. To run the demo, switch
to this directory and run:

```shell
docker-compose up -d
```

The demo exposes the following backends:

- Jaeger at http://0.0.0.0:16686
- Zipkin at http://0.0.0.0:9411
- Prometheus at http://0.0.0.0:9090 

Notes:

- It may take some time for the application metrics to appear on the Prometheus
 dashboard;

To clean up any docker container from the demo run `docker-compose down` from 
this directory.



docker run -p 13133 -p 55679-55680 -p 6060 -p 7276 -p 8888 -p 9411 -p 9943 \
    -v collector.yaml:/etc/collector.yaml:ro \
    --name otelcontribcol otel/opentelemetry-collector-contrib:0.13.0 \
        --config /etc/collector.yaml --mem-ballast-size-mib=683
