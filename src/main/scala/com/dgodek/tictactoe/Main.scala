package com.dgodek.tictactoe

import cats.effect.{ExitCode, IO, IOApp}
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val application = new Application()

    application.run().as(ExitCode.Success)
  }
}
