package com.dgodek.tictactoe.infrastructure.doobie

import cats.effect.{ContextShift, IO}
import cats.implicits.toTraverseOps
import cats.instances.list.catsStdInstancesForList
import com.dgodek.tictactoe.domain._
import com.dgodek.tictactoe.util.GameUtils
import doobie.syntax.connectionio.toConnectionIOOps
import doobie.util.transactor.Transactor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class GameQueryTest extends AnyFunSuite with doobie.scalatest.IOChecker with BeforeAndAfterAll {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  import GameUtils._

  override val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/tic_tac_toe",
    "username",
    "password"
  )

  test("execute create schema") { Schema.createTables.map(check(_)) }

  override protected def beforeAll(): Unit =
    Schema.createTables.map(_.run).sequence.transact(transactor).as(()).unsafeRunSync()

  val gameQuery = GameQuery(
    Some(firstPlayerId),
    Some(gameId)
  )

  test("execute insertGameQuery") { check(GameQueries.insertGame) }
  test("execute findGameQuery") { check(GameQueries.findGame(gameQuery)) }
  test("execute findGameWithMovesQuery") { check(GameQueries.findGameWithMoves(gameQuery)) }
  test("execute insertMoveQuery") { check(GameQueries.insertMove) }
  test("execute updateGameStatusQuery with empty playerId ") {
    check(GameQueries.updateGameStatus(gameId, GameStatus.Finished, winnerPlayerId = None))
  }
  test("execute updateGameStatusQuery with playerId") {
    check(GameQueries.updateGameStatus(gameId, GameStatus.Finished, winnerPlayerId = Some(firstPlayerId)))
  }
  test("execute updateSecondPlayerQuery") { check(GameQueries.updateSecondPlayerId(gameId, secondPlayerId)) }

}
