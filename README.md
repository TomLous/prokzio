# prokzio
#### A Zio gRPC/REST HTTP Proxy for Kafka

[TODO some badges]

This project is made as proof of concept to run highly performant, light weight gRPC/REST API based on Scala based on the [Zio](https://zio.dev/) framework


## Medium Articles
- [Day 0](https://tomlous.medium.com/building-an-open-source-scala-grpc-rest-http-proxy-for-kafka-i-d259ce6b9a20)
- [Day 1](https://tomlous.medium.com/building-an-open-source-scala-grpc-rest-http-proxy-for-kafka-ii-81f1c680fed3)


## Local

- `make graal-build-local service` Builds local binary of the service in a file `output/service`

- `make graal-build-docker-local service` Builds linux binary of the service, wraped in an alpine container in docker image called `service`

TODO add all make commands 

## About 

Created by [TomLous](https://github.com/TomLous/)