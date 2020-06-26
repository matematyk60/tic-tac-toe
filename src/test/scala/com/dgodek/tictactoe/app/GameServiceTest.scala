package com.dgodek.tictactoe.app

import cats.{~>, Id}
import com.dgodek.tictactoe.domain.GameErrors.JoinGameError
import com.dgodek.tictactoe.domain._
import com.dgodek.tictactoe.util.GameUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameServiceTest extends AnyFlatSpec with Matchers with MockFactory {

  import GameUtils._

  implicit val timeProviderMock: TimeProvider[Id]         = mock[TimeProvider[Id]]
  implicit val gameIdProviderMock: IdProvider[Id, GameId] = mock[IdProvider[Id, GameId]]
  implicit val gameRepositoryMock: GameRepository[Id]     = mock[GameRepository[Id]]
  implicit val gameFinishResolver: GameFinishResolver     = mock[GameFinishResolver]

  implicit val transformDbioToF: ~>[Id, Id] = new ~>[Id, Id] {
    override def apply[A](fa: Id[A]): Id[A] = fa
  }

  "GameService.createNewGame" should "save game and return gameId" in {
    (timeProviderMock.currentTime _).expects().returning(game.startTime.value)
    (gameIdProviderMock.generateId _).expects().returning(game.id)
    gameRepositoryMock.saveGame _ expects game returning ()

    GameService.createNewGame[Id, Id](firstPlayerId) shouldBe game.id
  }

  "GameService.joinGame" should "save second player if slot is free" in {
    gameRepositoryMock.findGameWithMoves _ expects GameQuery(gameId = Some(game.id)) returning Some(emptyGame)
    (gameRepositoryMock.updateSecondPlayerId _).expects(game.id, secondPlayerId) returning ()

    GameService.joinGame[Id, Id](secondPlayerId, game.id) shouldBe Right(game.id)
  }

  it should "return error if game does not exist" in {
    gameRepositoryMock.findGameWithMoves _ expects GameQuery(gameId = Some(game.id)) returning None

    GameService.joinGame[Id, Id](secondPlayerId, game.id) shouldBe Left(JoinGameError.NoSuchGame)
  }

  "GameService.makeMove" should "save first move in db" in {
    gameRepositoryMock.findGameWithMoves _ expects GameQuery(gameId = Some(game.id)) returning Some(emptyGame)
    val move = Move(
      game.id,
      SequenceNumber(0),
      firstPlayerId,
      Mark(Cords(xPosition = Position(0), yPosition = Position(0)), MarkType.Cross)
    )
    gameRepositoryMock.saveMove _ expects move returning ()
    (gameFinishResolver.isWinningMove _).expects(emptyGame.withNewMove(move), move.mark) returning false
    gameFinishResolver.isFull _ expects (emptyGame.withNewMove(move)) returning false

    GameService.makeMove[Id, Id](firstPlayerId, game.id, 0, 0) shouldBe Right(game.id)
  }

  it should "update status if move is winning" in {
    gameRepositoryMock.findGameWithMoves _ expects GameQuery(gameId = Some(game.id)) returning Some(emptyGame)
    val move = Move(
      game.id,
      SequenceNumber(0),
      firstPlayerId,
      Mark(Cords(xPosition = Position(0), yPosition = Position(0)), MarkType.Cross)
    )

    (gameFinishResolver.isWinningMove _).expects(emptyGame.withNewMove(move), move.mark) returning true

    gameRepositoryMock.saveMove _ expects move returning ()
    (gameRepositoryMock.updateGameStatusAndWinner _).expects(game.id, GameStatus.Finished, firstPlayerId)

    GameService.makeMove[Id, Id](firstPlayerId, game.id, 0, 0) shouldBe Right(game.id)
  }

  it should "update status if game will be tied" in {
    gameRepositoryMock.findGameWithMoves _ expects GameQuery(gameId = Some(game.id)) returning Some(emptyGame)
    val move = Move(
      game.id,
      SequenceNumber(0),
      firstPlayerId,
      Mark(Cords(xPosition = Position(0), yPosition = Position(0)), MarkType.Cross)
    )

    (gameFinishResolver.isWinningMove _).expects(emptyGame.withNewMove(move), move.mark) returning false
    gameFinishResolver.isFull _ expects emptyGame.withNewMove(move) returning true

    gameRepositoryMock.saveMove _ expects move returning ()
    (gameRepositoryMock.updateGameStatus _).expects(game.id, GameStatus.Tied)

    GameService.makeMove[Id, Id](firstPlayerId, game.id, 0, 0) shouldBe Right(game.id)
  }

}
