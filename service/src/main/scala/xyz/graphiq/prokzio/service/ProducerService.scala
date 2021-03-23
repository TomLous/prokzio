package xyz.graphiq.prokzio.service

import io.grpc.Status
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList
import zio.{Ref, ZEnv, ZIO}
import zio.stream.ZStream
import zio.console._
import xyz.graphiq.prokzio.service.grpc.{ProducerRequest, ProducerResponse, ZioGrpc}

class ProducerService extends ZioGrpc.ZProkzioService[ZEnv, Any] {
  override def produce(request: ProducerRequest): ZIO[ZEnv with Any, Status, ProducerResponse] = ???
}
