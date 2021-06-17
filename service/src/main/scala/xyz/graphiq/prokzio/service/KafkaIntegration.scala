package xyz.graphiq.prokzio.service

import org.apache.kafka.clients.producer.internals.ProducerMetadata
import zio._
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.serialization.{Serializer => KafkaSerializer}
import zio.blocking.Blocking

case class KafkaIntegration[K, M](producerSettings: List[String])(
    keySerializer: KafkaSerializer[K],
    messageSerializer: KafkaSerializer[M]
) {

  private lazy val kafkaProducer: ZManaged[Any, Throwable, Producer.Service[Any, K, M]] =
    Producer.make(
      ProducerSettings(producerSettings),
      Serializer(keySerializer),
      Serializer(messageSerializer)
    )

//  private lazy val kafkaProducerLayer: ZLayer[Any, Throwable, Has[Producer.Service[Any, K, M]]] =
//    ZLayer.fromManaged(kafkaProducer)

  private def toProducerRecord(topic: String, key: K, message: M): ProducerRecord[K, M] =
    new ProducerRecord(topic, key, message)

  def send(
      topic: String,
      key: K,
      message: M
  ): IO[Throwable, RecordMetadata] = {
    kafkaProducer.use(_.produce(toProducerRecord(topic, key, message)))

//    Producer
//      .produce(toProducerRecord(topic, key, message))
//      .provideSomeLayer(kafkaProducerLayer)
  }

  //      .provideSome(kafkaProducer)

//  def send(
//      topic: String,
//      key: String,
//      message: String
//  ): IO[KafkaException, ProducerMetadata] = { env =>
//    val x =
//      Producer
//        .produce(new ProducerRecord(topic, key, message))
//        .provideSomeLayer(kafkaProducer)
//        .provideSomeLayer(env)
//    UIO(x)
//  }
}
