package xyz.graphiq.prokzio.service

import zio._

object HelloWorld extends zio.App {

  def run(args: List[String]) =
    myAppLogic.exitCode

  val task = Task(println(s"Hello, world from thread: ${Thread.currentThread().getName}!"))

  val myAppLogic =
    for {
      fiber <- task.fork
      _ <- task
      _ <- fiber.join
    } yield ()
}
