package xyz.graphiq.prokzio.service

import io.grpc.Status
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.{ByteArraySerializer, BytesSerializer}
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList
import zio.{Ref, ZEnv, ZIO}
import zio.stream.ZStream
import zio.console._
import xyz.graphiq.prokzio.service.grpc.{ProducerRequest, ProducerResponse, ZioGrpc}

class ProducerService extends ZioGrpc.ZProkzioService[ZEnv, Any] {

  lazy val kafkaIntegration = KafkaIntegration(Nil)(ByteArraySerializer, ByteArraySerializer)

  override def produce(request: ProducerRequest): ZIO[ZEnv with Any, Status, ProducerResponse] = {
    kafkaIntegration
      .send(request.topic, request.recordKey.get.toByteArray, request.record.toByteArray)
      .bimap(_ => Status.UNAVAILABLE, metaDataToResponse)
  }

  def metaDataToResponse(metaData: RecordMetadata): ProducerResponse = ???

  def dummySend
}
