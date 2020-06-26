package com.dgodek.tictactoe

import java.time.LocalDateTime

import cats.effect.{Blocker, ContextShift, IO, Resource, Timer}
import cats.implicits.toTraverseOps
import cats.instances.list.catsStdInstancesForList
import cats.~>
import com.dgodek.tictactoe.api.GameRoutes
import com.dgodek.tictactoe.domain.{GameFinishResolver, GameId, GameRepository, IdProvider, TimeProvider}
import com.dgodek.tictactoe.infrastructure.doobie.{DoobieGameRepository, Schema}
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import doobie.implicits._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import scala.util.Random

class Application(
    implicit contextShift: ContextShift[IO],
    ec: ExecutionContext,
    timer: Timer[IO]
) {

  implicit val ioTimeProvider: TimeProvider[IO] = new TimeProvider[IO] {
    override def currentTime(): IO[LocalDateTime] = IO(LocalDateTime.now())
  }
  implicit val gameIdProvider: IdProvider[IO, GameId] = new IdProvider[IO, GameId] {
    override def generateId(): IO[GameId] = IO(Random.nextInt(100)).map(GameId)
  }

  implicit val gameFinishResolver: GameFinishResolver = GameFinishResolver

  def run(): IO[Unit] = transactorResource.use { xa =>
    implicit val transactorTransformer: ~>[ConnectionIO, IO] = new ~>[ConnectionIO, IO] {
      override def apply[A](fa: ConnectionIO[A]): IO[A] = xa.trans.apply(fa)
    }

    implicit val gameRepository: GameRepository[ConnectionIO] = DoobieGameRepository

    val gameRoutes = GameRoutes.routes[IO, ConnectionIO]

    val router = Router("/" -> gameRoutes).orNotFound

    val createSchema       = Schema.createTables.map(_.run).sequence.transact(xa).as(())
    val insertDefaultUsers = Schema.insertDefaultUsers.run.transact(xa).as(())

    val runServer = BlazeServerBuilder
      .apply[IO](ec)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(router)
      .serve
      .compile
      .drain

    createSchema *> insertDefaultUsers *> runServer
  }

  private def transactorResource: Resource[IO, Transactor[IO]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](8)
      bl <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/tic_tac_toe",
        user = "username",
        pass = "password",
        connectEC = ec,
        blocker = bl
      )
    } yield xa
}
